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
package dk.dma.embryo.dataformats.job;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.common.log.EmbryoLogFactory;
import dk.dma.embryo.common.log.EmbryoLogService;
import dk.dma.embryo.common.mail.MailSender;
import dk.dma.embryo.common.util.NamedtimeStamps;
import dk.dma.embryo.dataformats.model.ShapeFileMeasurement;
import dk.dma.embryo.dataformats.model.factory.ShapeFileNameParser;
import dk.dma.embryo.dataformats.model.factory.ShapeFileNameParserFactory;
import dk.dma.embryo.dataformats.persistence.ShapeFileMeasurementDao;
import dk.dma.embryo.dataformats.service.ShapeFileService;

@Singleton
@Startup
public class ShapeFileMeasurerJob {
    private static long TRANSACTION_LENGTH = 60 * 1000L * 4;

    private final Logger logger = LoggerFactory.getLogger(ShapeFileMeasurerJob.class);

    @Inject
    private ShapeFileService service;

    @Inject
    private ShapeFileMeasurementDao shapeFileMeasurementDao;

    @Inject
    private PropertyFileService propertyFileService;

    @Inject
    private MailSender mailSender;

    @Inject
    @Property(value = "embryo.iceChart.providers")
    private Map<String, String> iceChartProviders;
    
    @Inject
    @Property(value = "embryo.iceberg.providers")
    private Map<String, String> icebergProviders;

    private Map<String, String> directories = new HashMap<>();

    private NamedtimeStamps notifications = new NamedtimeStamps();

    @Resource
    private TimerService timerService;

    @Inject
    @Property(value = "embryo.shapeFileMeasurer.cron", substituteSystemProperties = true)
    private ScheduleExpression cron;

    @Inject
    private EmbryoLogFactory embryoLogFactory;

    @Inject
    private ShapeFileNameParserFactory parserFactory;

    private List<String> requiredFilesInIceObservation = Arrays.asList(".prj", ".dbf", ".shp", ".shx");

    @PostConstruct
    public void init() throws IOException {
        if (cron != null) {
            logger.info("Initializing {} with {}", this.getClass().getSimpleName(), cron.toString());
            initProvider(iceChartProviders, "iceChart");
            initProvider(icebergProviders, "iceberg");
            logger.info("Initializing {} with {}", this.getClass().getSimpleName(), directories.toString());
            timerService.createCalendarTimer(cron, new TimerConfig(null, false));
        } else {
            logger.info("Cron job not scheduled.");
        }
    }
    
    private void initProvider(Map<String, String> providers, String chartType) {
        for (String providerKey : providers.keySet()) {
        String property = "embryo." + chartType + "." + providerKey + ".localDirectory";
        String value = propertyFileService.getProperty(property, true);
        if (value != null) {
            directories.put(chartType + "-" + providerKey, value);
        } else {
            logger.info("Property {} not found", property);
        }
        }
    }

    private boolean isIceObservationFullyDownloaded(String directory, String name) {
        for (String suffix : requiredFilesInIceObservation) {
            if (!new File(directory + "/" + name + suffix).exists()) {
                return false;
            }
        }

        return true;
    }

    private Set<String> downloadedIceObservations(String directory) {
        Set<String> result = new HashSet<>();
        File[] files = new File(directory).listFiles();
        if (files != null) {
            for (File f : files) {
                String name = f.getName().substring(0, f.getName().indexOf("."));
                if (isIceObservationFullyDownloaded(directory, name)) {
                    result.add(name);
                }
            }
        }
        return result;
    }

    @Timeout
    public void measureFiles() {
        long start = new Date().getTime();

        logger.info("Measuring files ... (transaction length " + TRANSACTION_LENGTH + " msec.)");

        notifications.clearOldThanMinutes(60 * 24);
        
        for (Entry<String, String> directory : directories.entrySet()) {
            String key = directory.getKey();
            String chartType = key.substring(0, key.indexOf("-"));
            String provider = key.substring(key.indexOf("-") + 1);

            EmbryoLogService embryoLogger = embryoLogFactory.getLogger(this.getClass(), provider);

            try {
                ShapeFileMeasurer measurer = new ShapeFileMeasurer(service, shapeFileMeasurementDao, embryoLogger,
                        parserFactory);
                measurer.measureFiles(directory.getValue(), chartType, provider, start);
                List<ShapeFileMeasurement> measurements = measurer.getMeasurements();
                logger.debug("{}: Done. Saving {} items ...", provider, measurements.size());

                logger.debug("Calling deleteAll({})", provider);
                shapeFileMeasurementDao.deleteAll(chartType, provider);

                logger.info("{}: Saving {} measurements", provider, measurements.size());

                for (ShapeFileMeasurement sfm : measurements) {
                    shapeFileMeasurementDao.saveEntity(sfm);
                }

                String msg = measurer.getExistingCount() + " files validated. " + measurer.getNewCount()
                        + " new files measured. ";
                if (measurer.getFailedMeasurementCount() > 0) {
                    embryoLogger.error(msg + measurer.getFailedMeasurementCount() + " failed measurements.");
                } else {
                    embryoLogger.info(msg);
                }

            } catch (Throwable t) {
                embryoLogger.error("Unhandled error measuring shape files: " + t, t);
            }
        }
    }

    private class ShapeFileMeasurer {
        private int newMeasurements;
        private int existingMeasurements;
        private int failedMeasurementCount;

        private Map<String, ShapeFileMeasurement> measurements = new HashMap<>();

        private final ShapeFileService service;
        private final ShapeFileMeasurementDao dao;
        private final EmbryoLogService embryoLogger;
        private final ShapeFileNameParserFactory parserFactory;

        public ShapeFileMeasurer(ShapeFileService service, ShapeFileMeasurementDao dao,
                EmbryoLogService embryoLogService, ShapeFileNameParserFactory parserFactory) {
            super();
            this.service = service;
            this.dao = dao;
            this.embryoLogger = embryoLogService;
            this.parserFactory = parserFactory;
        }

        private long measureFile(String pfn) throws IOException {
            ObjectMapper mapper = new ObjectMapper();
            ShapeFileService.Shape file = service.readSingleFile(pfn, null, "", true, null, 0);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPOutputStream gos = new GZIPOutputStream(out);
            String result = mapper.writeValueAsString(file);
            gos.write(result.getBytes());
            gos.close();

            return out.toByteArray().length;
        }

        private void sendEmail(String chartType, String provider, String chartName, Throwable t) {
            String to = propertyFileService.getProperty("embryo." + chartType + "." + provider + ".notification.email");
            if (to != null && to.trim().length() > 0 && !notifications.contains(chartName)) {
                new ShapeNotificationMail(provider, chartName, t, propertyFileService).send(mailSender);
                notifications.add(chartName, DateTime.now(DateTimeZone.UTC));
            }
        }

        public void measureFiles(String directory, String chartType, String provider, long start) throws IOException {
            for (String fn : downloadedIceObservations(directory)) {

                ShapeFileNameParser parser = parserFactory.createParser(provider);
                ShapeFileMeasurement sfm = parser.parse(chartType, fn);

                ShapeFileMeasurement lookup = dao.lookup(sfm.getFileName(), chartType, provider);
                if (lookup == null || lookup.getVersion() < sfm.getVersion()) {
                    logger.debug("" + (new Date().getTime() - start) + " vs " + TRANSACTION_LENGTH);

                    if (System.currentTimeMillis() - start < TRANSACTION_LENGTH) {
                        logger.debug("Measuring file: " + fn);

                        try {
                            sfm.setFileSize(measureFile(chartType + "-" + provider + "." + fn));
                            sfm.setCreated(DateTime.now(DateTimeZone.UTC));
                            logger.debug("File size: " + sfm.getFileSize());
                            measurements.put(sfm.getFileName(), sfm);
                            newMeasurements++;
                        } catch (Throwable t) {
                            failedMeasurementCount++;
                            logger.error("Error measuring " + fn + ": " + t, t);
                            embryoLogger.error("Error measuring " + fn + ": " + t, t);
                            sendEmail(chartType, provider, fn, t);
                        }
                    }
                } else if (!measurements.containsKey(lookup.getFileName())) {
                    sfm.setFileName(lookup.getFileName());
                    sfm.setFileSize(lookup.getFileSize());
                    sfm.setProvider(lookup.getProvider());
                    sfm.setVersion(lookup.getVersion());
                    sfm.setCreated(lookup.getCreated());

                    measurements.put(sfm.getFileName(), sfm);
                    existingMeasurements++;
                }
            }
        }

        public List<ShapeFileMeasurement> getMeasurements() {
            List<ShapeFileMeasurement> result = new ArrayList<>();
            result.addAll(measurements.values());
            return result;
        }

        public int getNewCount() {
            return newMeasurements;
        }

        public int getExistingCount() {
            return existingMeasurements;
        }

        public int getFailedMeasurementCount() {
            return failedMeasurementCount;
        }
    }

}
