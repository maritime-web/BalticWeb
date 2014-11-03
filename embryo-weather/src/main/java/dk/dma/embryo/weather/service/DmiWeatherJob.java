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
package dk.dma.embryo.weather.service;

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.log.EmbryoLogService;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
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
import java.util.List;

/**
 * 
 * @author Jesper Tejlgaard
 */
@Singleton
@Startup
public class DmiWeatherJob {

    @Inject
    private Logger logger;

    @Resource
    private TimerService timerService;

    @Inject
    private EmbryoLogService embryoLogService;
    
    @Inject
    private WeatherServiceImpl weatherService;

    @Inject
    @Property("embryo.weather.dmi.ftp.cron")
    private ScheduleExpression cron;

    @Inject
    @Property("embryo.weather.dmi.ftp.serverName")
    private String dmiServer;

    @Inject
    @Property("embryo.weather.dmi.ftp.login")
    private String dmiLogin;

    @Inject
    @Property("embryo.weather.dmi.ftp.password")
    private String dmiPassword;

    @Inject
    @Property(value = "embryo.weather.dmi.localDirectory", substituteSystemProperties = true)
    private String localDmiDir;

    @Inject
    @Property(value = "embryo.tmpDir", substituteSystemProperties = true)
    private String tmpDir;

    public DmiWeatherJob() {
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

    @Timeout
    public void timeout() {
        // notifications.clearOldThanMinutes(silencePeriod);

        try {
            logger.info("Making directories if necessary ...");

            if (!new File(localDmiDir).exists()) {
                logger.info("Making local directory for DMI files: " + localDmiDir);
                new File(localDmiDir).mkdirs();
            }

            FTPClient ftp = connect();

            logger.info("Transfer files ...");
            final List<String> transfered = new ArrayList<>();
            final List<String> error = new ArrayList<>();

            try {
                List<FTPFile> files = Arrays.asList(ftp.listFiles(null, DmiFTPFileFilters.FILES));
                for (FTPFile file : files) {
                    try {
                        if (transferFile(ftp, file, localDmiDir)) {
                            transfered.add(file.getName());
                        }
                    } catch (RuntimeException e) {
                        error.add(file.getName());
                    }
                }
            } finally {
                ftp.logout();
            }
            
            try {
                weatherService.refresh();
            }catch(Exception e){
                embryoLogService.error("Error reading transfered file", e);
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

        File tmpFile = new File(tmpDir, "dmiWeather" + Math.random());

        FileOutputStream fos = new FileOutputStream(tmpFile);

        try {
            logger.info("Transfering " + file.getName() + " to " + tmpFile.getAbsolutePath());
            if (!ftp.retrieveFile(file.getName(), fos)) {
                Thread.sleep(10);
                if (tmpFile.exists()) {
                    logger.info("Deleting temporary file " + tmpFile.getAbsolutePath());
                    tmpFile.delete();
                }

                throw new RuntimeException("File transfer failed (" + file.getName() + ")");
            }
        } finally {
            fos.close();
        }

        Thread.sleep(10);

        Path dest = Paths.get(localDmiDir).resolve(file.getName());
        logger.info("Moving " + tmpFile + " to " + dest.getFileName());
        Files.move(Paths.get(tmpFile.getAbsolutePath()), dest, StandardCopyOption.REPLACE_EXISTING);

        return true;
    }

}
