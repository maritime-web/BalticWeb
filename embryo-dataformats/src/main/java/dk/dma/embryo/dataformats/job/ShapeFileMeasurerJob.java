/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
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

import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.common.log.EmbryoLogFactory;
import dk.dma.embryo.common.log.EmbryoLogService;
import dk.dma.embryo.common.mail.MailSender;
import dk.dma.embryo.dataformats.model.ShapeFileMeasurement;
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
    private Map<String, String> providers;

    private Map<String, String> directories = new HashMap<>();

    private NamedTimeStamps notifications = new NamedTimeStamps();

    @Resource
    private TimerService timerService;

    @Inject
    @Property(value = "embryo.shapeFileMeasurer.cron", substituteSystemProperties = true)
    private ScheduleExpression cron;

    @Inject
    private EmbryoLogFactory embryoLogFactory;

    private List<String> requiredFilesInIceObservation = Arrays.asList(".prj", ".dbf", ".shp", ".shx");

    @PostConstruct
    public void init() throws IOException {
        if (cron != null) {
            logger.info("Initializing {} with {}", this.getClass().getSimpleName(), cron.toString());
            for (String providerKey : providers.keySet()) {
                String property = "embryo.iceChart." + providerKey + ".localDirectory";
                String value = propertyFileService.getProperty(property, true);
                if (value != null) {
                    directories.put(providerKey, value);
                } else {
                    logger.info("Property {} not found", property);
                }
            }
            logger.info("Initializing {} with {}", this.getClass().getSimpleName(), directories.toString());
            timerService.createCalendarTimer(cron, new TimerConfig(null, false));
        } else {
            logger.info("Cron job not scheduled.");
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

        notifications.clearOldThanMinutes(60*24);

        for (Entry<String, String> directory : directories.entrySet()) {
            String provider = directory.getKey();

            EmbryoLogService embryoLogger = embryoLogFactory.getLogger(this.getClass(), provider);

            try {
                ShapeFileMeasurer measurer = new ShapeFileMeasurer(service, shapeFileMeasurementDao, embryoLogger);
                measurer.measureFiles(directory.getValue(), provider, start);
                List<ShapeFileMeasurement> measurements = measurer.getMeasurements();
                logger.debug("{}: Done. Saving {} items ...", provider, measurements.size());

                logger.debug("Calling deleteAll({})", provider);
                shapeFileMeasurementDao.deleteAll(provider);

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

        private List<ShapeFileMeasurement> measurements = new ArrayList<>(100);

        private final ShapeFileService service;
        private final ShapeFileMeasurementDao dao;
        private final EmbryoLogService embryoLogger;

        public ShapeFileMeasurer(ShapeFileService service, ShapeFileMeasurementDao dao,
                EmbryoLogService embryoLogService) {
            super();
            this.service = service;
            this.dao = dao;
            this.embryoLogger = embryoLogService;
        }

        private long measureFile(String pfn) throws IOException {
            ObjectMapper mapper = new ObjectMapper();
            ShapeFileService.Shape file = service.readSingleFile(pfn, 0, "", true, 3, 0);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPOutputStream gos = new GZIPOutputStream(out);
            String result = mapper.writeValueAsString(file);
            gos.write(result.getBytes());
            gos.close();

            return out.toByteArray().length;
        }

        private void sendEmail(String provider, String chartName, Throwable t) {
            String to = propertyFileService.getProperty("embryo.iceChart." + provider + ".notification.email");
            if (to != null && to.trim().length() > 0 && !notifications.contains(chartName)) {
                new ShapeNotificationMail(provider, chartName, t, propertyFileService).send(mailSender);
                notifications.add(chartName, DateTime.now(DateTimeZone.UTC));
            }
        }

        public void measureFiles(String directory, String provider, long start) throws IOException {
            for (String fn : downloadedIceObservations(directory)) {

                ShapeFileMeasurement lookup = dao.lookup(fn, provider);
                if (lookup == null) {
                    logger.debug("" + (new Date().getTime() - start) + " vs " + TRANSACTION_LENGTH);

                    if (System.currentTimeMillis() - start < TRANSACTION_LENGTH) {
                        logger.debug("Measuring file: " + fn);

                        try {
                            ShapeFileMeasurement sfm = new ShapeFileMeasurement();
                            sfm.setFileName(fn);
                            sfm.setFileSize(measureFile(provider + "." + fn));
                            sfm.setProvider(provider);
                            logger.debug("File size: " + sfm.getFileSize());
                            measurements.add(sfm);
                            newMeasurements++;
                        } catch (Throwable t) {
                            failedMeasurementCount++;
                            logger.error("Error measuring " + fn + ": " + t, t);
                            embryoLogger.error("Error measuring " + fn + ": " + t, t);
                            sendEmail(provider, fn, t);
                        }
                    }
                } else {
                    ShapeFileMeasurement sfm = new ShapeFileMeasurement();

                    sfm.setFileName(lookup.getFileName());
                    sfm.setFileSize(lookup.getFileSize());
                    sfm.setProvider(lookup.getProvider());

                    measurements.add(sfm);
                    existingMeasurements++;
                }
            }
        }

        public List<ShapeFileMeasurement> getMeasurements() {
            return measurements;
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

    public static class NamedTimeStamps {
        private Map<String, DateTime> notifications = new HashMap<>();

        public void clearOldThanMinutes(int minutes) {
            DateTime now = DateTime.now(DateTimeZone.UTC);

            List<String> toDelete = new ArrayList<>(notifications.size());

            for (Entry<String, DateTime> entry : notifications.entrySet()) {
                if (entry.getValue().plusMinutes(minutes).isBefore(now)) {
                    toDelete.add(entry.getKey());
                }
            }

            for (String name : toDelete) {
                notifications.remove(name);
            }
        }

        public boolean contains(String name) {
            return notifications.containsKey(name);
        }

        public void add(String name, DateTime ts) {
            notifications.put(name, ts);
        }
    }
}
