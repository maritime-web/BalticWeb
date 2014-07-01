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
package dk.dma.embryo.weather.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
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
import org.slf4j.Logger;

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.log.EmbryoLogService;

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
        String fn = System.getProperty("java.io.tmpdir") + "/test" + Math.random();
        FileOutputStream fos = new FileOutputStream(fn);

        try {
            logger.info("Transfering " + file.getName() + " to " + fn);
            if (!ftp.retrieveFile(file.getName(), fos)) {
                throw new RuntimeException("File transfer failed (" + file.getName() + ")");
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

}
