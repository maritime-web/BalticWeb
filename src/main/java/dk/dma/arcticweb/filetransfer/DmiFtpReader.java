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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.arcticweb.service.EmbryoLogService;
import dk.dma.configuration.Property;

@Singleton
@Startup
public class DmiFtpReader {
    private String[] charts = {
            "CapeFarewell_RIC",
            "CentralWest_RIC",
            "Greenland_WA",
            "NorthEast_RIC",
            "NorthWest_RIC",
            "Qaanaaq_RIC",
            "SouthEast_RIC",
            "SouthWest_RIC"
    };

    private final Logger logger = LoggerFactory.getLogger(DmiFtpReader.class);

    @Inject
    @Property("embryo.iceMaps.cron")
    private ScheduleExpression cron;
    @Inject
    @Property("embryo.iceMaps.dmiFtpServerName")
    private String dmiServer;
    @Inject
    @Property("embryo.iceMaps.dmiFtpLogin")
    private String dmiLogin;
    @Inject
    @Property("embryo.iceMaps.dmiFtpPassword")
    private String dmiPassword;
    @Inject
    @Property("embryo.iceMaps.dmiFtpBaseDirectory")
    private String dmiBaseDirectory;
    @Inject
    @Property(value = "embryo.iceMaps.localDmiDirectory", substituteSystemProperties = true)
    private String localDmiDirectory;
    @Inject
    @Property("embryo.iceMaps.ftp.ageInDays")
    private Integer ageInDays;
    
    @Resource
    private TimerService timerService;

    @Inject
    private EmbryoLogService embryoLogService;

    private List<String> requiredFilesInIceObservation = Arrays.asList(".prj", ".dbf", ".shp", ".shp.xml", ".shx");

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
        try {
            logger.info("Making directory if necessary ...");
            if (!new File(localDmiDirectory).exists()) {
                logger.info("Making local directort for DMI files: " + localDmiDirectory);
                new File(localDmiDirectory).mkdirs();
            }
            logger.info("Calling transfer files ...");
            int count = transferFiles();
            embryoLogService.info("Scanned DMI (" + dmiServer + ") for new files. Files transferred: " + count);
        } catch (Throwable t) {
            logger.error("Unhandled error scanning/transfering files from DMI (" + dmiServer + "): " + t, t);
            embryoLogService.error("Unhandled error scanning/transfering files from DMI (" + dmiServer + "): " + t, t);
        }
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        logger.info("Shutdown called.");
    }

    private boolean isIceObservationFullyDownloaded(String name) {
        for (String suffix : requiredFilesInIceObservation) {
            if (!new File(localDmiDirectory + "/" + name + suffix).exists()) {
                return false;
            }
        }

        return true;
    }

    private boolean filter(String fn, LocalDate limit) {
        boolean result = false;

        for (String c : charts) {
            result |= fn.endsWith(c);
        }

        if(!result){
            return false;
        }
        
        try {
            Date date = new SimpleDateFormat("yyyyMMddHHmm").parse(fn.substring(0, 12));
            
            LocalDateTime mapDate = new LocalDateTime(date.getTime());
            
            return mapDate.toLocalDate().isAfter(limit) ;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public int transferFiles() throws IOException, InterruptedException {
        int count = 0;

        FTPClient ftp = new FTPClient();
        logger.info("Connecting to " + dmiServer + " using " + dmiLogin + " ...");

        ftp.setDefaultTimeout(30000);
        ftp.connect(dmiServer);
        ftp.login(dmiLogin, dmiPassword);
        ftp.enterLocalPassiveMode();
        ftp.setFileType(FTP.BINARY_FILE_TYPE);

        try {
            ftp.changeWorkingDirectory(dmiBaseDirectory);

            List<String> subdirectoriesAtServer = new ArrayList<String>();
            
            LocalDate mapsYoungerThan = LocalDate.now().minusDays(ageInDays).minusDays(1);

            Thread.sleep(10);

            logger.info("Reading files in: " + dmiBaseDirectory);

            for (FTPFile f : ftp.listFiles()) {
                if (filter(f.getName(), mapsYoungerThan) && !isIceObservationFullyDownloaded(f.getName())) {
                    subdirectoriesAtServer.add(f.getName());
                }
            }

            for (String subdirectory : subdirectoriesAtServer) {
                Thread.sleep(10);

                logger.info("Reading files from subdirectories: " + subdirectory);

                ftp.changeWorkingDirectory(subdirectory);

                List<String> filesInSubdirectory = new ArrayList<>();

                for (FTPFile f : ftp.listFiles()) {
                    filesInSubdirectory.add(f.getName());
                }

                for (String fn : filesInSubdirectory) {
                    for (String prefix : requiredFilesInIceObservation) {
                        if (fn.endsWith(prefix)) {
                            if (transferFile(ftp, fn)) {
                                count++;
                            }
                        }
                    }
                }

                ftp.changeToParentDirectory();
            }

        } finally {
            ftp.logout();
        }

        return count;
    }

    private boolean transferFile(FTPClient ftp, String name) throws IOException, InterruptedException {
        String localName = localDmiDirectory + "/" + name;

        if (new File(localName).exists()) {
            logger.info("Not transfering " + name + " since the file already exists in " + localName);
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
}
