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

import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;
import static dk.dma.embryo.tiles.service.DmiSatellitePredicates.dateIsAfter;
import static dk.dma.embryo.tiles.service.DmiSatellitePredicates.downloaded;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.slf4j.Logger;

import com.google.common.collect.Collections2;

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.common.log.EmbryoLogService;
import dk.dma.embryo.common.mail.MailSender;
import dk.dma.embryo.common.util.NamedtimeStamps;

/**
 * @author Jesper Tejlgaard
 */
@Singleton
@Startup
public class DmiSatelliteJob {

    @Inject
    private Logger logger;

    @Resource
    private TimerService timerService;

    @Inject
    private EmbryoLogService embryoLogService;

    @Inject
    private PropertyFileService propertyFileService;

    @Inject
    private MailSender mailSender;

    @Inject
    @Property("embryo.tiles.providers.dmi.ftp.cron")
    private ScheduleExpression cron;

    @Inject
    @Property("embryo.tiles.providers.dmi.ftp.serverName")
    private String dmiServer;

    @Inject
    @Property("embryo.tiles.providers.dmi.ftp.login")
    private String dmiLogin;

    @Inject
    @Property("embryo.tiles.providers.dmi.ftp.password")
    private String dmiPassword;

    @Inject
    @Property("embryo.tiles.providers.dmi.ftp.baseDirectory")
    private String baseDir;

    @Inject
    @Property("embryo.tiles.ageInDays")
    private Integer ageInDays;

    @Inject
    @Property("embryo.tiles.providers.dmi.ftp.daysToKeepFiles")
    private Integer daysToKeepFiles;
/*

    @Inject
    @Property("embryo.inshoreIceReport.dmi.notification.silenceperiod")
    private Integer silencePeriod;

    @Inject
    @Property("embryo.inshoreIceReport.dmi.notification.email")
    private String mailTo;
*/

    @Inject
    @Property(value = "embryo.tiles.providers.dmi.types.satellite-ice.localDirectory", substituteSystemProperties = true)
    private String localDmiDir;

    @Inject
    @Property(value = "embryo.tmpDir", substituteSystemProperties = true)
    private String tmpDir;

    private NamedtimeStamps notifications = new NamedtimeStamps();

    public DmiSatelliteJob() {
    }

    @PostConstruct
    public void init() {
        if (!dmiServer.trim().equals("") && (cron != null)) {
            logger.info("Initializing {} with {}", this.getClass().getSimpleName(), cron.toString());
            timerService.createCalendarTimer(cron, new TimerConfig(null, false));
        } else {
            logger.info("DMI FTP site is not configured - cron job not scheduled.");
        }
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        logger.info("Shutdown called.");
    }

    @Timeout
    public void timeout() {
        //notifications.clearOldThanMinutes(silencePeriod);

        try {
            logger.info("Making directories if necessary ...");

            if (!new File(localDmiDir).exists()) {
                logger.info("Making local directory for DMI files: " + localDmiDir);
                new File(localDmiDir).mkdirs();
            }

            DateTime mapsYoungerThan = DateTime.now(DateTimeZone.UTC).minusDays(ageInDays - 1).minusDays(1);
            DateTime deleteOlderThan = daysToKeepFiles == -1 ? null : DateTime.now(DateTimeZone.UTC).minusDays(daysToKeepFiles);
            Result result = new Result();

            Set<String> existingFiles = alreadyDownloadedFiles();

            FTPClient ftp = connect();

            logger.info("Transfer files ...");
            final List<String> transfered = new ArrayList<>();
            final List<String> error = new ArrayList<>();

            try {
                if (!ftp.changeWorkingDirectory(baseDir)) {
                    throw new IOException("Could not change to base directory:" + baseDir);
                }

                List<FTPFile> directories = Arrays.asList(ftp.listFiles(null, FTPFileFilters.DIRECTORIES));

                logger.debug("Directories: {}", directories);

                // TODO validate directories format, e.g. NASA-Modis
                for (FTPFile dir : directories) {
                    String namePrefix = dir.getName().replace("-", "_") + "_";

                    List<FTPFile> files = Arrays.asList(ftp.listFiles(dir.getName(), EmbryoFTPFileFilters.FILES));

                    Collection<FTPFile> toDelete;
                    Collection<FTPFile> notDownloaded;
                    if (deleteOlderThan != null) {
                        toDelete = Collections2.filter(files, not(dateIsAfter(deleteOlderThan)));
                        logger.debug("To delete: {}", toDelete);
                        // Check both against days before delete and allowed age in days
                        // Reasons is that one day someone will configure
                        // embryo.tiles.providers.dmi.ftp.baseDirectory = 5
                        // embryo.tiles.ageInDays = 10
                        // which with the current code result in an attempt to download already deleted files
                        notDownloaded = Collections2.filter(files, and(dateIsAfter(deleteOlderThan), dateIsAfter(mapsYoungerThan), not(downloaded(namePrefix, existingFiles))));
                    } else {
                        toDelete = Collections.emptyList();
                        notDownloaded = Collections2.filter(files, and(dateIsAfter(mapsYoungerThan), not(downloaded(namePrefix, existingFiles))));
                    }
                    logger.debug("Not downloaded files: {}", notDownloaded);

                    if (notDownloaded.size() > 0 || toDelete.size() > 0) {
                        String directory = dir.getName();
                        if (!ftp.changeWorkingDirectory(directory)) {
                            throw new IOException("Could not change to directory:" + directory);
                        }

                        result.merge(delete(ftp, toDelete));

                        for (FTPFile file : notDownloaded) {
                            try {
                                if (transferFile(ftp, file, namePrefix, localDmiDir)) {
                                    transfered.add(file.getName());
                                } else {
                                    error.add(file.getName());
                                }
                            } catch (RuntimeException e) {
                                error.add(file.getName());
                            }
                        }

                        if (!ftp.changeToParentDirectory()) {
                            throw new IOException("Could not change to parent directory:" + baseDir);
                        }
                    }
                }

//                sendEmails(rejected);
            } finally {
                ftp.logout();
            }

            String msg = "Scanned DMI (" + dmiServer + ") for files. Transfered: " + toString(transfered)
                    + ", Errors: " + toString(error) + ", Delete failed: " + toString(result.failedDelete) + ", Deleted: " + result.deleted;
            if (error.size() == 0) {
                logger.info(msg);
                embryoLogService.info(msg);
            } else {
                logger.error(msg);
                embryoLogService.error(msg);
            }
        } catch (Exception t) {
            logger.error("Unhandled error scanning/transfering files from DMI (" + dmiServer + "): " + t, t);
            embryoLogService.error("Unhandled error scanning/transfering files from DMI (" + dmiServer + "): " + t, t);
        }
    }

    String toString(List<String> list) {
        StringBuilder builder = new StringBuilder();

        for (String str : list) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(str);
        }

        return builder.toString();
    }

    FTPClient connect() throws IOException {
        FTPClient ftp = new FTPClient();
        logger.info("Connecting to " + dmiServer + " using " + dmiLogin + " ...");

        ftp.setDefaultTimeout(30000);
        ftp.connect(dmiServer);
        ftp.login(dmiLogin, dmiPassword);
        ftp.enterLocalPassiveMode();
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
        return ftp;
    }


    private boolean transferFile(FTPClient ftp, FTPFile file, String namePrefix, String localDmiDir) throws IOException,
            InterruptedException {
        File tmpFile = new File(tmpDir, "dmiSatellite" + Math.random());
        FileOutputStream fos = new FileOutputStream(tmpFile);

        try {
            logger.info("Transfering " + file.getName() + " to " + tmpFile.getAbsolutePath());
            if (!ftp.retrieveFile(file.getName(), fos)) {
                Thread.sleep(10);
                if (tmpFile.exists()) {
                    logger.info("Deleting temporary file " + tmpFile.getAbsolutePath());
                    tmpFile.delete();
                }

                return false;
            }
        } finally {
            fos.close();
        }

        Thread.sleep(10);

        Path dest = Paths.get(localDmiDir).resolve(namePrefix + file.getName());
        logger.info("Moving " + tmpFile.getAbsolutePath() + " to " + dest.getFileName());
        Files.move(Paths.get(tmpFile.getAbsolutePath()), dest, StandardCopyOption.REPLACE_EXISTING);

        return true;
    }

    /*
    private void sendEmail(String fileName, Exception cause) {
        String key = fileName + cause.getMessage();
        if (mailTo != null && mailTo.trim().length() > 0 && !notifications.contains(key)) {
            new InshoreIceReportFileNotReadMail("dmi", fileName, cause, propertyFileService)
                    .send(mailSender);
            notifications.add(key, DateTime.now(DateTimeZone.UTC));
        }
    }

    private void sendEmails(Collection<FTPFile> rejected) {
        for (FTPFile file : rejected) {
            if (mailTo != null && mailTo.trim().length() > 0 && !notifications.contains(file.getName())) {
                new InshoreIceReportNameNotAcceptedMail("dmi", file.getName(), propertyFileService)
                        .send(mailSender);
                notifications.add(file.getName(), DateTime.now(DateTimeZone.UTC));
            }
        }      
    }*/

    Set<String> alreadyDownloadedFiles() {
        Set<String> existingFiles = new HashSet<>();

        File localDirectory = new File(localDmiDir);
        File[] files = localDirectory.listFiles();
        for (File file : files) {
            existingFiles.add(file.getName());
        }
        return existingFiles;
    }

    public Result delete(FTPClient ftp, Collection<FTPFile> toDelete) {
        Result result = new Result();

        for (FTPFile file : toDelete) {
            try {
                if (ftp.deleteFile(file.getName())) {
                    result.deleted++;
                } else {
                    result.failedDelete.add(file.getName());
                }
            } catch (Exception e) {
                result.failedDelete.add(file.getName());
            }
        }
        return result;
    }
}
