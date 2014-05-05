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

import static com.google.common.base.Predicates.not;
import static dk.dma.embryo.dataformats.job.DmiIceChartPredicates.acceptedIceCharts;
import static dk.dma.embryo.dataformats.job.DmiIceChartPredicates.validFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import javax.inject.Inject;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilters;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.common.log.EmbryoLogService;
import dk.dma.embryo.common.mail.MailSender;
import dk.dma.embryo.dataformats.model.ShapeFileMeasurement;
import dk.dma.embryo.dataformats.persistence.ShapeFileMeasurementDao;

@Singleton
@Startup
public class DmiFtpReaderJob {

    private final Logger logger = LoggerFactory.getLogger(DmiFtpReaderJob.class);

    @Inject
    @Property("embryo.iceChart.dmi.cron")
    private ScheduleExpression cron;
    @Inject
    @Property("embryo.iceChart.dmi.regions")
    private Map<String, String> regions;
    @Inject
    @Property("embryo.iceChart.dmi.ftp.serverName")
    private String dmiServer;
    @Inject
    @Property("embryo.iceChart.dmi.ftp.login")
    private String dmiLogin;
    @Inject
    @Property("embryo.iceChart.dmi.ftp.password")
    private String dmiPassword;
    @Inject
    @Property("embryo.iceChart.dmi.ftp.baseDirectory")
    private String dmiBaseDirectory;
    @Inject
    @Property(value = "embryo.iceChart.dmi.localDirectory", substituteSystemProperties = true)
    private String localDmiDir;
    @Inject
    @Property("embryo.iceChart.dmi.ftp.ageInDays")
    private Integer ageInDays;

    @Inject
    @Property("embryo.iceChart.dmi.notification.email")
    private String mailTo;

    @Inject
    @Property("embryo.iceChart.dmi.notification.silenceperiod")
    private Integer silencePeriod;

    @Resource
    private TimerService timerService;

    @Inject
    private EmbryoLogService embryoLogService;

    @Inject
    private MailSender mailSender;

    @Inject
    private ShapeFileMeasurementDao shapeFileMeasurementDao;

    @Inject
    private PropertyFileService propertyFileService;

    private String[] iceChartExts = new String[] { ".prj", ".dbf", ".shp", ".shx" };

    private NamedtimeStamps notifications = new NamedtimeStamps();

    @PostConstruct
    public void init() {
        if (!dmiServer.trim().equals("") && (cron != null)) {
            logger.info("Initializing {} with {}", this.getClass().getSimpleName(), cron.toString());
            logger.info("Initializing {} with localDirectory {} and regions {}", this.getClass().getSimpleName(),
                    localDmiDir, regions);
            timerService.createCalendarTimer(cron, new TimerConfig(null, false));
        } else {
            logger.info("DMI FTP site is not configured - cron job not scheduled.");
        }
    }

    @Timeout
    public void timeout() {
        notifications.clearOldThanMinutes(silencePeriod);

        try {
            logger.info("Making directory if necessary ...");
            if (!new File(localDmiDir).exists()) {
                logger.info("Making local directory for DMI files: " + localDmiDir);
                new File(localDmiDir).mkdirs();
            }
            logger.info("Calling transfer files ...");
            Counts counts = transferFiles();
            String msg = "Scanned DMI (" + dmiServer + ") for files. Transfered: " + counts.transferCount
                    + ", Shapes deleted: " + counts.shapeDeleteCount + ". Files deleted: " + counts.fileDeleteCount
                    + ", Errors: " + counts.errorCount;
            if (counts.errorCount == 0) {
                logger.info(msg);
                embryoLogService.info(msg);
            } else {
                logger.error(msg);
                embryoLogService.error(msg);
            }
        } catch (Throwable t) {
            logger.error("Unhandled error scanning/transfering files from DMI (" + dmiServer + "): " + t, t);
            embryoLogService.error("Unhandled error scanning/transfering files from DMI (" + dmiServer + "): " + t, t);
        }
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        logger.info("Shutdown called.");
    }

    private void sendEmail(String chartName) {
        if (mailTo != null && mailTo.trim().length() > 0 && !notifications.contains(chartName)) {
            new IceChartNameNotAcceptedMail("dmi", chartName, regions.keySet(), propertyFileService).send(mailSender);
            notifications.add(chartName, DateTime.now(DateTimeZone.UTC));
        }
    }

    public Counts transferFiles() throws IOException, InterruptedException {
        Counts counts = new Counts();
        final List<String> subdirectoriesAtServer = new ArrayList<String>();

        FTPClient ftp = new FTPClient();
        logger.info("Connecting to " + dmiServer + " using " + dmiLogin + " ...");

        ftp.setDefaultTimeout(30000);
        ftp.connect(dmiServer);
        ftp.login(dmiLogin, dmiPassword);
        ftp.enterLocalPassiveMode();
        ftp.setFileType(FTP.BINARY_FILE_TYPE);

        try {
            if (!ftp.changeWorkingDirectory(dmiBaseDirectory)) {
                throw new IOException("Could not change to base directory:" + dmiBaseDirectory);
            }

            LocalDate mapsYoungerThan = LocalDate.now().minusDays(ageInDays).minusDays(15);

            Thread.sleep(10);

            logger.info("Reading files in: " + dmiBaseDirectory);

            List<FTPFile> allDirs = Arrays.asList(ftp.listFiles(null, FTPFileFilters.DIRECTORIES));
            Collection<FTPFile> rejected = Collections2.filter(allDirs, not(validFormat(regions.keySet())));
            Collection<FTPFile> accepted = Collections2.filter(allDirs,
                    acceptedIceCharts(regions.keySet(), mapsYoungerThan, localDmiDir, iceChartExts));

            subdirectoriesAtServer.addAll(Collections2.transform(allDirs, new NameFunction()));

            for (FTPFile file : rejected) {
                sendEmail(file.getName());
            }

            for (FTPFile subdirectory : accepted) {
                Thread.sleep(10);

                logger.info("Reading files from subdirectories: " + subdirectory.getName());

                ftp.changeWorkingDirectory(subdirectory.getName());

                List<String> filesInSubdirectory = new ArrayList<>();

                for (FTPFile f : ftp.listFiles()) {
                    filesInSubdirectory.add(f.getName());
                }

                for (String fn : filesInSubdirectory) {
                    for (String prefix : iceChartExts) {
                        if (fn.endsWith(prefix)) {
                            if (transferFile(ftp, fn)) {
                                counts.transferCount++;
                            }
                        }
                    }
                }
                ftp.changeToParentDirectory();
            }
        } finally {
            ftp.logout();
        }

        logger.info("Deleting Shape entries no longer existing on FTP");
        List<ShapeFileMeasurement> dmiMeasurements = shapeFileMeasurementDao.list("dmi");
        for (ShapeFileMeasurement measurement : dmiMeasurements) {
            if (!subdirectoriesAtServer.contains(measurement.getFileName())) {
                try {
                    logger.info("Deleting shape entry {}", measurement.getFileName());
                    shapeFileMeasurementDao.remove(measurement);
                    counts.shapeDeleteCount++;
                } catch (Exception e) {
                    String msg = "Error deleting shape entry " + measurement.getFileName() + " from ArcticWeb server";
                    logger.error(msg, e);
                    embryoLogService.error(msg, e);
                    counts.errorCount++;
                }
            }
        }

        FileUtility fileService = new FileUtility(localDmiDir);
        String[] filesToDelete = fileService.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                String[] parts = name.split("\\.");
                return !subdirectoriesAtServer.contains(parts[0]);
            }
        });

        for (String file : filesToDelete) {
            try {
                logger.info("Deleting chart files {}", file);
                fileService.deleteFile(file);
                counts.fileDeleteCount++;
            } catch (Exception e) {
                String msg = "Error deleting chart file " + file + " from ArcticWeb server";
                logger.error(msg, e);
                embryoLogService.error(msg, e);
                counts.errorCount++;
            }
        }
        return counts;
    }

    private boolean transferFile(FTPClient ftp, String name) throws IOException, InterruptedException {
        String localName = localDmiDir + "/" + name;

        if (new File(localName).exists()) {
            logger.debug("Not transfering " + name + " since the file already exists in " + localName);
            return false;
        }

        String fn = System.getProperty("java.io.tmpdir") + "/test" + Math.random();

        FileOutputStream fos = new FileOutputStream(fn);

        try {
            logger.info("Transfering " + name + " to " + fn);
            if (!ftp.retrieveFile(name, fos)) {
                throw new RuntimeException("File transfer failed (" + name + ")");
            }
        } finally {
            fos.close();
        }

        Thread.sleep(10);

        logger.info("Moving " + fn + " to " + localName);
        new File(fn).renameTo(new File(localName));

        return true;
    }

    public class NameFunction implements Function<FTPFile, String> {
        @Override
        public String apply(FTPFile input) {
            return input.getName();
        }

    }

    private class Counts {
        int transferCount;
        int shapeDeleteCount;
        int fileDeleteCount;
        int errorCount;
    }
}
