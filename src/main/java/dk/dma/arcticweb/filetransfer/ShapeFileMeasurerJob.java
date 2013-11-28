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
package dk.dma.arcticweb.filetransfer;

import dk.dma.arcticweb.dao.ShapeFileMeasurementDao;
import dk.dma.arcticweb.service.EmbryoLogService;
import dk.dma.configuration.Property;
import dk.dma.embryo.domain.ShapeFileMeasurement;
import dk.dma.embryo.rest.ShapeFileService;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

@Singleton
@Startup
public class ShapeFileMeasurerJob {
    private static long TRANSACTION_LENGTH = 60 * 1000L * 7;

    private final Logger logger = LoggerFactory.getLogger(ShapeFileMeasurerJob.class);

    @Inject
    private ShapeFileService service;

    @Inject
    private ShapeFileMeasurementDao shapeFileMeasurementDao;

    @Inject
    @Property(value = "embryo.iceMaps.localDmiDirectory", substituteSystemProperties = true)
    private String localDmiDirectory;

    @Resource
    private TimerService timerService;

    @Inject
    @Property(value = "embryo.shapeFileMeasurer.cron", substituteSystemProperties = true)
    private ScheduleExpression cron;

    @Inject
    private EmbryoLogService embryoLogService;

    private List<String> requiredFilesInIceObservation = Arrays.asList(".prj", ".dbf", ".shp", ".shp.xml", ".shx");

    private String prefix = "dmi.";

    @PostConstruct
    public void init() throws IOException {
        if (cron != null) {
            timerService.createCalendarTimer(cron, new TimerConfig(null, false));
        } else {
            logger.info("Cron job not scheduled.");
        }
    }

    private boolean isIceObservationFullyDownloaded(String name) {
        for (String suffix : requiredFilesInIceObservation) {
            if (!new File(localDmiDirectory + "/" + name + suffix).exists()) {
                return false;
            }
        }

        return true;
    }

    private Set<String> downloadedIceObservations() {
        Set<String> result = new HashSet<>();
        File[] files = new File(localDmiDirectory).listFiles();
        if (files != null) {
            for (File f : files) {
                String name = f.getName().substring(0, f.getName().indexOf("."));
                if (isIceObservationFullyDownloaded(name)) {
                    result.add(name);
                }
            }
        }
        return result;
    }

    @Timeout
    public void measureFiles() throws IOException {
        try {
            int count = 0;

            long start = System.currentTimeMillis();

            logger.info("Measuring files ...");

            List<ShapeFileMeasurement> measurements = new ArrayList<>();

            for (String fn : downloadedIceObservations()) {
                ShapeFileMeasurement lookup = shapeFileMeasurementDao.lookup(fn, prefix);
                if (lookup == null) {
                    if (System.currentTimeMillis() - start < TRANSACTION_LENGTH) {
                        logger.info("Measuring file: " + fn);

                        ShapeFileMeasurement sfm = new ShapeFileMeasurement();

                        sfm.setFileName(fn);
                        sfm.setFileSize(measureFile(fn));
                        sfm.setPrefix(prefix);

                        logger.info("File size: " + sfm.getFileSize());

                        measurements.add(sfm);

                        count++;
                    }
                } else {
                    ShapeFileMeasurement sfm = new ShapeFileMeasurement();

                    sfm.setFileName(lookup.getFileName());
                    sfm.setFileSize(lookup.getFileSize());
                    sfm.setPrefix(lookup.getPrefix());

                    measurements.add(sfm);
                }
            }

            logger.info("Done. Saving " + measurements.size() + " items ...");

            logger.info("Calling deleteAll");

            shapeFileMeasurementDao.deleteAll(prefix);

            logger.info("Saving " + measurements.size() + " measurements");

            for (ShapeFileMeasurement sfm : measurements) {
                shapeFileMeasurementDao.saveEntity(sfm);
            }

            embryoLogService.info(measurements.size() + " files validated / measured. " + count + " new.");
        } catch (Throwable t) {
            embryoLogService.error("Unhandled error measuring shape files: " + t, t);
        }
    }

    private long measureFile(String fn) throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<ShapeFileService.Shape> file = service.getMultipleFile(prefix + fn, 0, "", true, 3);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPOutputStream gos = new GZIPOutputStream(out);
            String result = mapper.writeValueAsString(file);
            gos.write(result.getBytes());
            gos.close();

            return out.toByteArray().length;

        } catch (Throwable t) {
            logger.info("Error measuring " + fn + ": " + t, t);
            embryoLogService.error("Error measuring " + fn + ": " + t, t);
            return -1;
        }
    }
}
