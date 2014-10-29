/*
 * Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dk.dma.embryo.tiles.service;

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.common.configuration.Provider;
import dk.dma.embryo.common.configuration.Type;
import dk.dma.embryo.common.log.EmbryoLogService;
import dk.dma.embryo.common.mail.MailSender;
import dk.dma.embryo.common.util.NamedtimeStamps;
import dk.dma.embryo.tiles.model.TileSet;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jesper Tejlgaard on 8/26/14.
 */
@Singleton
@Startup
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class TilerJob {
    @Inject
    private Logger logger;

    @Inject
    @Property(value = "embryo.tiles.maxConcurrentJobs")
    private Integer maxConcurrentJobs;

    @Inject
    @Property(value = "embryo.tiles.cron", substituteSystemProperties = true)
    private ScheduleExpression cron;

    private NamedtimeStamps notifications = new NamedtimeStamps();

    private List<Provider> providers;

    @Resource
    TimerService timerService;

    @Inject
    private EmbryoLogService embryoLogService;

    @Inject
    private TileSetDao tileSetDao;

    @Inject
    private TilerService tilerService;

    @Inject
    private SourceFileNameParser fileNameParser;

    @Inject
    private PropertyFileService propertyFileService;

    @Inject
    private MailSender mailSender;


    @PostConstruct
    public void init() throws IOException {
        if (timerService == null) {
            logger.error("Timerservice not available. Skipping initialization");
            return;
        }

        providers = propertyFileService.getProvidersProperty("embryo.tiles.providers");

        if (cron != null && providers.size() > 0) {
            logger.info("Initializing {} with {}", this.getClass().getSimpleName(), cron.toString());
            logger.info("Initializing {} with providers {}", this.getClass().getSimpleName(), providers);
            timerService.createCalendarTimer(cron, new TimerConfig(null, false));
        } else {
            logger.info("Cron job not scheduled.");
        }
    }

    @Timeout
    public void convertToTiles() {
        try {
            logger.debug("Max concurrent jobs: {}", maxConcurrentJobs);

            notifications.clearOldThanMinutes(60 * 24);

            Result result = new Result();
            for (Provider provider : providers) {
                result.merge(convertToTilesForProvider(provider));
            }

            String msg = "Started new " + result.jobsStarted + " jobs. Detected " + result.errorCount + " errors";
            if (result.errorCount > 0) {
                logger.error(msg);
                embryoLogService.error(msg);
            } else {
                logger.debug(msg);
                embryoLogService.info(msg);
            }
        } catch (Exception e) {
            String msg = "Fatal error tiling geo referenced images";
            logger.error(msg, e);
            embryoLogService.error(msg, e);
        }
    }

    private Result convertToTilesForProvider(Provider provider) throws Exception {
        Result result = new Result();
        for (Type type : provider.getTypes()) {
            try {
                List<TileSet> images = tileSetDao.listByProviderAndType(provider.getShortName(), type.getName());
                Map<String, TileSet> imageMap = toMap(images);
                logger.debug("imageMap: {}", imageMap);

                File directory = new File(type.getLocalDirectory());
                if (!directory.exists()) {
                    if (!directory.mkdirs()) {
                        // Do something else.
                        throw new IOException("Failed creating directory " + directory);
                    }
                }
                File[] files = directory.listFiles(new ImageTypeFilter());

                // TODO validate download of jpg, prj and jtw?

                logger.debug("Files: " + files);

                for (File file : files) {
                    TileSet tileSet = fileNameParser.parse(file);
                    if (!imageMap.containsKey(tileSet.getName())) {
                        tileSet.setProvider(provider.getShortName());
                        tileSet.setType(type.getName());
                        tileSetDao.saveEntity(tileSet);
                    }
                }
            } catch (Exception e) {
                String msg = "Fatal error tiling geo referenced images of type " + type.getName() + " for provider " + provider.getShortName();
                logger.error(msg, e);
                embryoLogService.error(msg, e);
                result.errorCount++;
            }
        }

        for (Type type : provider.getTypes()) {
            try {
                int concurrentJobs = tileSetDao.listByStatus(TileSet.Status.CONVERTING).size();
                if (concurrentJobs >= maxConcurrentJobs) {
                    logger.debug("{} (concurrentJobs) >= {} (maxConcurrentjobs), skipping upstart of new jobs", concurrentJobs, maxConcurrentJobs);
                    return result;
                }
                File directory = new File(type.getLocalDirectory());
                File[] files = directory.listFiles(new ImageTypeFilter());
                result = startTileJobs(provider.getShortName(), type.getName(), files, concurrentJobs, result);
            } catch (Exception e) {
                String msg = "Fatal error tiling geo referenced images of type " + type.getName() + " for provider " + provider.getShortName();
                logger.error(msg, e);
                embryoLogService.error(msg, e);
                result.errorCount++;
            }
        }
        return result;
    }


    Result startTileJobs(String provider, String type, File[] files, int concurrentJobs, Result result) {
        List<TileSet> unconverted = tileSetDao.listByProviderAndTypeAndStatus(provider, type, TileSet.Status.UNCONVERTED);
        Map<String, TileSet> tileSetMap = toMap(unconverted);

        for (int index = 0; index < files.length && concurrentJobs < maxConcurrentJobs; index++) {
            File file = files[index];
            String name = ImageType.getName(file);
            if (tileSetMap.containsKey(name)) {
                tilerService.transformGeotiff2tiles(file.getAbsoluteFile(), name, provider);
                concurrentJobs++;
                result.jobsStarted++;
            }
        }

        return result;
    }

    private Map<String, TileSet> toMap(List<TileSet> images) {
        Map<String, TileSet> result = new HashMap();
        for (TileSet image : images) {
            result.put(image.getName(), image);
        }
        return result;
    }

    static class Result {
        int jobsStarted;
        int errorCount;

        public Result merge(Result res) {
            Result newResult = new Result();
            newResult.errorCount = this.errorCount + res.errorCount;
            newResult.errorCount = this.jobsStarted + res.jobsStarted;
            return newResult;
        }
    }

    static class ImageTypeFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            return ImageType.getType(pathname) != null;
        }
    }
}
