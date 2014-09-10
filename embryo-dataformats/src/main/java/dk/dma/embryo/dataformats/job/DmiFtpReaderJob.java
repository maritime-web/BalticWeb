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
import java.util.Map.Entry;

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
    /*
     * @Inject
     * 
     * @Property("embryo.iceChart.dmi.regions") private Map<String, String>
     * regions;
     */
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
    /*
     * @Inject
     * 
     * @Property(value = "embryo.iceChart.dmi.localDirectory",
     * substituteSystemProperties = true) private String localDmiDir;
     */
    @Inject
    @Property("embryo.iceChart.dmi.ftp.ageInDays")
    private Integer ageInDays;

    @Inject
    @Property("embryo.ice.charttypes")
    private Map<String, String> charttypes;

    @Inject
    @Property("embryo.ftp.dirtypes")
    private Map<String, String> dirtypes;

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
            String[] localDirs = new String[charttypes.size()];
            List<String> regions = new ArrayList<>();
            int count = 0;
            for (String chartType : charttypes.values()) {
                localDirs[count] = getLocalDmiDir(chartType);
                Map<String, String> regionsForChartType = getRegions(chartType);
                regions.addAll(regionsForChartType.keySet());
            }
            logger.info("Initializing {} with localDirectories {} and regions {}", this.getClass().getSimpleName(), localDirs, regions);
            timerService.createCalendarTimer(cron, new TimerConfig(null, false));
        } else {
            logger.info("DMI FTP site is not configured - cron job not scheduled.");
        }
    }

    @Timeout
    public void timeout() {
        notifications.clearOldThanMinutes(silencePeriod);

        try {
            logger.info("Making directories if necessary ...");
            for (String chartType : charttypes.values()) {
                String localDmiDir = getLocalDmiDir(chartType);
                if (!new File(localDmiDir).exists()) {
                    logger.info("Making local directory for DMI files: " + localDmiDir);
                    new File(localDmiDir).mkdirs();
                }
            }
            logger.info("Calling transfer files ...");
            Counts counts = transferFiles();
            String msg = "Scanned DMI (" + dmiServer + ") for files. Transfered: " + counts.transferCount + ", Shapes deleted: " + counts.shapeDeleteCount
                    + ". Files deleted: " + counts.fileDeleteCount + ", Errors: " + counts.errorCount;
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

    private void sendEmail(String chartName, String chartType) {
        if (mailTo != null && mailTo.trim().length() > 0 && !notifications.contains(chartName)) {
            new IceChartNameNotAcceptedMail("dmi", chartName, getRegions(chartType).keySet(), propertyFileService).send(mailSender);
            notifications.add(chartName, DateTime.now(DateTimeZone.UTC));
        }
    }

    private String getLocalDmiDir(String chartType) {
        return propertyFileService.getProperty("embryo." + chartType + ".dmi.localDirectory", true);
    }

    private Map<String, String> getRegions(String chartType) {
        return propertyFileService.getMapProperty("embryo." + chartType + ".dmi.regions");
    }

    private Counts readFromDmiTypedir(FTPFile typedir, FTPClient ftp, List<String> subdirectoriesAtServer) throws IOException, InterruptedException {
        Counts counts = new Counts();
        String chartType = charttypes.get(typedir.getName());
        String dirType = dirtypes.get(typedir.getName());
        String localDmiDir = getLocalDmiDir(chartType);
        Map<String, String> regions = getRegions(chartType);

        LocalDate mapsYoungerThan = LocalDate.now().minusDays(ageInDays).minusDays(15);

        Thread.sleep(10);

        logger.info("Reading files in: {}/{}", ftp.printWorkingDirectory(), typedir.getName());

        // Directories and single files should be handled differently.
        if (dirtypes != null) {
            if (dirType.equals(Dirtype.DIR.type)) {
                List<FTPFile> allDirs = Arrays.asList(ftp.listFiles(typedir.getName(), FTPFileFilters.DIRECTORIES));
                logger.debug("{}/{} contains files: {}", ftp.printWorkingDirectory(), typedir.getName(), allDirs);

                Collection<FTPFile> rejected = Collections2.filter(allDirs, not(validFormat(regions.keySet())));
                Collection<FTPFile> accepted = Collections2.filter(allDirs, acceptedIceCharts(regions.keySet(), mapsYoungerThan, localDmiDir, iceChartExts));

                logger.debug("rejected: {}", allDirs);
                logger.debug("accepted: {}", allDirs);

                subdirectoriesAtServer.addAll(Collections2.transform(allDirs, new NameFunction()));

                for (FTPFile file : rejected) {
                    sendEmail(file.getName(), chartType);
                }

                for (FTPFile subdirectory : accepted) {
                    Thread.sleep(10);

                    logger.info("Reading files from subdirectories: " + subdirectory.getName());

                    ftp.changeWorkingDirectory(typedir.getName() + "/" + subdirectory.getName());

                    List<String> filesInSubdirectory = new ArrayList<>();

                    for (FTPFile f : ftp.listFiles()) {
                        filesInSubdirectory.add(f.getName());
                    }

                    for (String fn : filesInSubdirectory) {
                        for (String prefix : iceChartExts) {
                            if (fn.endsWith(prefix)) {
                                if (transferFile(ftp, fn, localDmiDir)) {
                                    counts.transferCount++;
                                }
                            }
                        }
                    }
                    ftp.changeToParentDirectory();
                    ftp.changeToParentDirectory();
                }
            } else {
                List<FTPFile> allFiles = Arrays.asList(ftp.listFiles(typedir.getName(), FTPFileFilters.NON_NULL));
                ftp.changeWorkingDirectory(typedir.getName());
                for (FTPFile f : allFiles) {
                    // TODO: At this point, everything gets accepted. We might
                    // want to do some file name validation.
                    if (transferFile(ftp, f.getName(), localDmiDir)) {
                        counts.transferCount++;
                    }
                }
            }
        } else {
            logger.info(typedir.getName() + " not found in config file, ignoring directory.");
        }

        return counts;
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

            List<FTPFile> typeDirs = Arrays.asList(ftp.listFiles(null, FTPFileFilters.DIRECTORIES));
            for (FTPFile typedir : typeDirs) {
                logger.info("Reading from DMI dir " + typedir.getName());
                Counts c = readFromDmiTypedir(typedir, ftp, subdirectoriesAtServer);
                counts.addCounts(c);
            }
        } finally {
            ftp.logout();
        }

        logger.info("Deleting Shape entries no longer existing on FTP");
        List<ShapeFileMeasurement> dmiMeasurements = shapeFileMeasurementDao.list("iceChart", "dmi");
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

        for (Entry<String, String> entry : charttypes.entrySet()) {
            String chartType = entry.getValue();
            final String dirtype = dirtypes.get(entry.getKey());
            FileUtility fileService = new FileUtility(getLocalDmiDir(chartType));
            String[] filesToDelete = fileService.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    String[] parts = name.split("\\.");
                    // Note that only files in subdirectories (like ice charts
                    // and icebergs) are removed.
                    return dirtype.equals(Dirtype.DIR.type) && !subdirectoriesAtServer.contains(parts[0]);
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
        }
        return counts;
    }

    private boolean transferFile(FTPClient ftp, String name, String localDmiDir) throws IOException, InterruptedException {
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

        void addCounts(Counts other) {
            transferCount += other.transferCount;
            shapeDeleteCount += other.shapeDeleteCount;
            fileDeleteCount += other.fileDeleteCount;
            errorCount += other.errorCount;
        }
    }

    private static enum Dirtype {
        DIR("dir"), FILE("file");

        private String type;

        private Dirtype(String type) {
            this.type = type;
        }
    }
}
