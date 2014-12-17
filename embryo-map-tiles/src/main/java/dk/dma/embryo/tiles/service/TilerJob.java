/* Copyright (c) 2011 Danish Maritime Authority.
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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.common.configuration.Provider;
import dk.dma.embryo.common.configuration.Type;
import dk.dma.embryo.common.log.EmbryoLogService;
import dk.dma.embryo.common.mail.MailSender;
import dk.dma.embryo.common.util.NamedtimeStamps;
import dk.dma.embryo.tiles.image.ImageType;
import dk.dma.embryo.tiles.image.ImageTypeFilter;
import dk.dma.embryo.tiles.model.TileSet;

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
    private PropertyFileService propertyFileService;

    @Inject
    private MailSender mailSender;

    @Inject
    @Property("embryo.tiles.ageInDays")
    private Integer ageInDays;

    @Inject
    @Property(value = "embryo.tiles.directory", substituteSystemProperties = true)
    private String tilesDirectory;


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

    @PreDestroy
    public void shutdown() throws InterruptedException {
        logger.info("Shutdown called.");
    }

    @Timeout
    public void convertToTiles() {
        try {
            logger.debug("Max concurrent jobs: {}", maxConcurrentJobs);
            DateTime youngerThan = DateTime.now(DateTimeZone.UTC).minusDays(ageInDays).minusDays(1);

            notifications.clearOldThanMinutes(60 * 24);

            Result result = new Result();
            for (Provider provider : providers) {
                result = result.merge(deleteOldImages(provider, youngerThan));
                result = result.merge(markOldTileSetsForDeletion(provider, youngerThan));
                result = result.merge(deleteOldTileSets(provider, youngerThan.minusDays(2)));
                result = result.merge(deleteOldTiles(provider));
                result = result.merge(saveNewTileSetEntries(provider, youngerThan));
                result = result.merge(convertImagesToTiles(provider));
                result = result.merge(tilerService.cleanup());
            }

            String msg = "Started new " + result.jobsStarted + " jobs. Deleted " + result.deleted + ". Detected " + result.errorCount + " errors.";
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

    private Result deleteOldImages(Provider provider, DateTime limit) throws Exception {
        DeleteGeoImageVisitor visitor = new DeleteGeoImageVisitor(limit, tileSetDao, embryoLogService);
        provider.accept(visitor);
        return visitor.getResult();
    }

    private Result markOldTileSetsForDeletion(Provider provider, DateTime limit) {
        MarkTileSetsForDeletionVisitor visitor = new MarkTileSetsForDeletionVisitor(limit, tileSetDao, embryoLogService);
        provider.accept(visitor);
        return visitor.getResult();
    }

    private Result deleteOldTileSets(Provider provider, DateTime limit) {
        DeleteTileSetVisitor visitor = new DeleteTileSetVisitor(limit, tileSetDao, embryoLogService);
        provider.accept(visitor);
        return visitor.getResult();
    }

    private Result deleteOldTiles(Provider provider) {
        DeleteTilesVisitor visitor = new DeleteTilesVisitor(tileSetDao, embryoLogService, tilesDirectory);
        provider.accept(visitor);
        return visitor.getResult();
    }

    private Result saveNewTileSetEntries(Provider provider, DateTime limit) {
        SaveNewImagesAsTileSetsVisitor visitor = new SaveNewImagesAsTileSetsVisitor(limit, tileSetDao, embryoLogService);
        provider.accept(visitor);
        return visitor.getResult();
    }


    private Result convertImagesToTiles(Provider provider) throws Exception {
        Result result = new Result();
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
        Map<String, TileSet> tileSetMap = TileSet.toMap(unconverted);

        for (int index = 0; index < files.length && concurrentJobs < maxConcurrentJobs; index++) {
            File file = files[index];
            String name = ImageType.getName(file);
            if (tileSetMap.containsKey(name)) {
                tilerService.transformImage2tiles(file.getAbsoluteFile(), name, provider);
                concurrentJobs++;
                result.jobsStarted++;
            }
        }

        return result;
    }

}
