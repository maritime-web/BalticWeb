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
import java.io.IOException;
import java.util.ArrayList;
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

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.embryo.configuration.Property;
import dk.dma.embryo.service.EmbryoLogService;

@Singleton
@Startup
public class AariHttpReaderJob {

    private final Logger logger = LoggerFactory.getLogger(AariHttpReaderJob.class);

    @Inject
    @Property("embryo.iceChart.aari.cron")
    private ScheduleExpression cron;

    @Inject
    @Property("embryo.iceChart.aari.protocol")
    private String protocol;

    @Inject
    @Property("embryo.iceChart.aari.http.serverName")
    private String server;

    @Inject
    @Property("embryo.iceChart.aari.http.dataSets")
    private String dataSets;

    @Inject
    @Property("embryo.iceChart.aari.http.ageInDays")
    private Integer ageInDays;

    @Inject
    @Property("embryo.iceChart.aari.http.timeoutSeconds")
    private Integer timeout;

    @Inject
    @Property("embryo.iceChart.aari.regions")
    private Map<String, String> regions;

    @Inject
    @Property(value = "embryo.iceChart.aari.localDirectory", substituteSystemProperties = true)
    private String localDirectory;

    @Resource
    private TimerService timerService;

    @Inject
    private EmbryoLogService embryoLogService;

    @PostConstruct
    public void init() {
        if (!server.trim().equals("") && (cron != null)) {
            logger.info("Initializing {} with {}", this.getClass().getSimpleName(), cron.toString());
            timerService.createCalendarTimer(cron, new TimerConfig(null, false));
        } else {
            logger.info("AARI HTTP site is not configured - cron job not scheduled.");
        }
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        logger.info("Shutdown called.");
    }

    @Timeout
    public void transferFiles() {
        int fileCount = 0;
        int errorCount = 0;

        try {
            prepareLocalDirectory();
            File tmpDir = prepareTemporaryDirectory();

            Integer year = DateTime.now(DateTimeZone.UTC).getYear();
            
            logger.info("protocol={}, server={}, timeout={}", protocol, server, timeout);
            
            HttpReader reader = new HttpReader(protocol, server, timeout);

            for (String dataSet : dataSets.split(";")) {
                for (String region : regions.keySet()) {
                    String path = replaceVariables(dataSet, region, year);

                    List<String> files = null;
                    try {
                        logger.debug("Reading content in {}", path);
                        files = reader.readContent(path);
                    } catch (Exception e) {
                        files = new ArrayList<>(0);
                        errorCount++;
                        logger.error("Error reading folder {}", path);
                        embryoLogService
                                .error("Error reading folder  '" + path + "' from AARI (" + server + "): "
                                        + e.getMessage(), e);
                    }

                    for (String file : files) {
                        if (!isFileDownloaded(file)) {
                            try {
                                logger.debug("Transfering file {}/{}", path, file);
                                transferFile(reader, path, file, tmpDir);
                                fileCount++;
                            } catch (Exception e) {
                                errorCount++;
                                logger.error("Error transfering file {}/{}", path, file);
                                embryoLogService.error("Error transfering file '" + path + "/" + file + "' from AARI ("
                                        + server + "): " + e.getMessage(), e);
                            }
                        }

                    }
                }
            }
        } catch (Exception e) {
            logger.error("Unhandled error scanning/transfering files from AARI (" + server + "): " + e, e);
            embryoLogService.error("Unhandled error scanning/transfering files from AARI (" + server + "): " + e, e);
        }

        String msg = "Scanned AARI (" + server + ") for new files. Files transferred: " + fileCount;
        if (errorCount == 0) {
            embryoLogService.info(msg);
        } else {
            embryoLogService.error(msg + ". Transfer errors: " + errorCount);
        }
    }

    private void prepareLocalDirectory() {
        logger.info("Making directory if necessary ...");
        if (!new File(localDirectory).exists()) {
            logger.info("Making local directort for AARI files: " + localDirectory);
            new File(localDirectory).mkdirs();
        }
    }

    private File prepareTemporaryDirectory() {
        logger.info("Making temporary directory if necessary ...");
        File tmpDir = new File(System.getProperty("user.home") + "/arcticweb/tmp");
        if (!tmpDir.exists()) {
            logger.info("Making temporary directory " + tmpDir);
            tmpDir.mkdirs();
        }
        return tmpDir;
    }

    private void transferFile(HttpReader httpReader, String path, String fileName, File tmpDir)
            throws InterruptedException, IOException {
        File location = new File(tmpDir.getAbsoluteFile(), "" + Math.random());

        logger.info("Transfering " + fileName + " to " + location.getAbsolutePath());
        httpReader.getFile(path, fileName, location);
        Thread.sleep(10);

        String localName = localDirectory + "/" + fileName;
        logger.info("Moving " + location.getAbsolutePath() + " to " + localName);
        location.renameTo(new File(localName));
    }

    private boolean isFileDownloaded(String name) {
        return new File(localDirectory + "/" + name).exists();
    }

    private String replaceVariables(String path, String region, Integer year) {
        path = path.replaceAll("\\{yyyy\\}", year.toString());
        path = path.replaceAll("\\{region\\}", region);
        return path;
    }

}
