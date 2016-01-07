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
package dk.dma.embryo.dataformats.inshore;

import com.google.common.collect.Collections2;
import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.common.log.EmbryoLogService;
import dk.dma.embryo.common.mail.MailSender;
import dk.dma.embryo.common.util.NamedtimeStamps;
import dk.dma.embryo.dataformats.job.EmbryoFTPFileFilters;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.slf4j.Logger;

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
import java.util.List;
import java.util.Map;

import static dk.dma.embryo.dataformats.inshore.DmiInshoreIceReportPredicates.acceptedReports;
import static dk.dma.embryo.dataformats.inshore.DmiInshoreIceReportPredicates.rejectedReports;

/**
 *
 * @author Jesper Tejlgaard
 */
@Singleton
@Startup
public class DmiInshoreIceReportJob {

    @Inject
    private Logger logger;

    @Resource
    private TimerService timerService;

    @Inject
    private EmbryoLogService embryoLogService;

    @Inject
    private InshoreIceReportService iceInformationService;

    @Inject
    private PropertyFileService propertyFileService;

    @Inject
    private MailSender mailSender;

    @Inject
    @Property("embryo.inshoreIceReport.dmi.cron")
    private ScheduleExpression cron;

    @Inject
    @Property("embryo.inshoreIceReport.dmi.ftp.serverName")
    private String dmiServer;

    @Inject
    @Property("embryo.inshoreIceReport.dmi.ftp.login")
    private String dmiLogin;

    @Inject
    @Property("embryo.inshoreIceReport.dmi.ftp.password")
    private String dmiPassword;

    @Inject
    @Property("embryo.inshoreIceReport.dmi.ftp.baseDirectory")
    private String baseDir;

    @Inject
    @Property("embryo.inshoreIceReport.dmi.ftp.ageInDays")
    private Integer ageInDays;

    @Inject
    @Property("embryo.inshoreIceReport.dmi.notification.silenceperiod")
    private Integer silencePeriod;

    @Inject
    @Property("embryo.inshoreIceReport.dmi.notification.email")
    private String mailTo;

    @Inject
    @Property(value = "embryo.inshoreIceReport.dmi.localDirectory", substituteSystemProperties = true)
    private String localDmiDir;

    @Inject
    @Property(value = "embryo.tmpDir", substituteSystemProperties = true)
    private String tmpDir;


    private NamedtimeStamps notifications = new NamedtimeStamps();

    public DmiInshoreIceReportJob() {
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
        notifications.clearOldThanMinutes(silencePeriod);

        try {
            logger.info("Making directories if necessary ...");

            if (!new File(localDmiDir).exists()) {
                logger.info("Making local directory for DMI files: " + localDmiDir);
                new File(localDmiDir).mkdirs();
            }

            if (!new File(tmpDir).exists()) {
                logger.info("Making local directory for temporary files: " + tmpDir);
                new File(tmpDir).mkdirs();
            }


            LocalDate mapsYoungerThan = LocalDate.now().minusDays(ageInDays).minusDays(1);

            FTPClient ftp = connect();

            logger.info("Transfer files ...");
            final List<String> transfered = new ArrayList<>();
            final List<String> error = new ArrayList<>();

            try {
                if (!ftp.changeWorkingDirectory(baseDir)) {
                    throw new IOException("Could not change to base directory:" + baseDir);
                }

                List<FTPFile> files = Arrays.asList(ftp.listFiles(null, EmbryoFTPFileFilters.FILES));

                logger.debug("files: {}" , files);

                Collection<FTPFile> rejected = Collections2.filter(files, rejectedReports());
                Collection<FTPFile> accepted = Collections2.filter(files, acceptedReports(mapsYoungerThan));

                logger.debug("rejected: {}", rejected);

                for (FTPFile file : accepted) {
                    try {
                        if (transferFile(ftp, file, localDmiDir)) {
                            transfered.add(file.getName());
                        }
                    } catch (RuntimeException e) {
                        error.add(file.getName());
                    }
                }

                sendEmails(rejected);
            } finally {
                ftp.logout();
            }

            try {
                iceInformationService.update();
            } catch(InshoreIceReportException iire){
                for(Map.Entry<String, Exception> entry : iire.getCauses().entrySet()){
                    embryoLogService.error("Error reading transfered file " + entry.getKey(), entry.getValue());
                    sendEmail(entry.getKey(), entry.getValue());
                }
            } catch(Exception e){
                embryoLogService.error("Error reading transfered file(s)", e);
                error.add(e.getMessage());
            }

            String msg = "Scanned DMI (" + dmiServer + ") for files. Transfered: " + toString(transfered)
                    + ", Errors: " + toString(error);
            if (error.size() == 0) {
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

    private boolean transferFile(FTPClient ftp, FTPFile file, String localDmiDir) throws IOException,
            InterruptedException {

        String fn = tmpDir + "/inshore" + Math.random();
        FileOutputStream fos = new FileOutputStream(fn);

        try {
            logger.info("Transfering " + file.getName() + " to " + fn);
            if (!ftp.retrieveFile(file.getName(), fos)) {
                return false;
            }
        } finally {
            fos.close();
        }

        Thread.sleep(10);

        Path dest = Paths.get(localDmiDir).resolve(file.getName());
        logger.info("Moving " + fn + " to " + dest.getFileName());
        Files.move(Paths.get(fn), dest, StandardCopyOption.REPLACE_EXISTING);

        return true;
    }

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
    }

}
