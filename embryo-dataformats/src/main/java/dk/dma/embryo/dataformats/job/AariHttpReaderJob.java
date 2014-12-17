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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
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

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.common.log.EmbryoLogService;

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
    private PropertyFileService propertyService;

    @Inject
    @Property(value = "embryo.iceChart.aari.localDirectory", substituteSystemProperties = true)
    private String localDirectory;

    @Inject
    @Property(value = "embryo.tmpDir", substituteSystemProperties = true)
    private String tmpDir;

    @Resource
    private TimerService timerService;

    @Inject
    private EmbryoLogService embryoLogService;

    private HashSet<String> paths = new HashSet<>();

    
    public AariHttpReaderJob() {
        super();
    }
    
    public AariHttpReaderJob(ScheduleExpression cron, String server, String dataSets,
            PropertyFileService propertyService, TimerService timerService) {
        super();
        this.cron = cron;
        this.server = server;
        this.dataSets = dataSets;
        this.propertyService = propertyService;
        this.timerService = timerService;
    }

    @PostConstruct
    public void init() {
        if (!server.trim().equals("") && (cron != null)) {
            logger.info("Initializing {} with {}", this.getClass().getSimpleName(), cron.toString());
            
            for (String dataSet : dataSets.split(";")) {
                String path = propertyService.getProperty("embryo.iceChart.aari.http." + dataSet + ".path");
                String regions = propertyService.getProperty("embryo.iceChart.aari.http." + dataSet + ".regions");
                if (regions != null && !regions.isEmpty()) {
                    for (String region : regions.split(";")) {
                        paths.add(replaceRegions(path, region));
                    }
                } else {
                    paths.add(path);
                }
            }

            logger.info("Initializing {} with {}", this.getClass().getSimpleName(), paths);
            timerService.createCalendarTimer(cron, new TimerConfig(null, false));
        } else {
            logger.info("AARI HTTP site is not configured - cron job not scheduled.");
        }
    }
    
    HashSet<String> getPaths(){
        return paths;
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

            for (String p : paths) {
                String path = replaceYear(p, year);

                List<String> files = null;
                try {
                    logger.debug("Reading content in {}", path);
                    files = reader.readContent(path);
                } catch (Exception e) {
                    files = new ArrayList<>(0);
                    errorCount++;
                    logger.error("Error reading folder {}", path);
                    embryoLogService.error(
                            "Error reading folder  '" + path + "' from AARI (" + server + "): " + e.getMessage(), e);
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
        } catch (Exception e) {
            logger.error("Unhandled error scanning/transfering files from AARI (" + server + "): " + e, e);
            embryoLogService.error("Unhandled error scanning/transfering files from AARI (" + server + "): " + e, e);
        }

        String msg = "Scanned AARI (" + server + ") for new files. Files transferred: " + fileCount;
        if (errorCount == 0) {
            logger.info(msg);
            embryoLogService.info(msg);
        } else {
            logger.error(msg);
            embryoLogService.error(msg + ". Transfer errors: " + errorCount);
        }
    }

    private void prepareLocalDirectory() {
        logger.info("Making directory if necessary ...");
        if (!new File(localDirectory).exists()) {
            logger.info("Making local directory for AARI files: " + localDirectory);
            new File(localDirectory).mkdirs();
        }
    }

    private File prepareTemporaryDirectory() {
        logger.info("Making temporary directory if necessary ...");
        File tmpDirectory = new File(tmpDir);
        if (!tmpDirectory.exists()) {
            logger.info("Making temporary directory " + tmpDir);
            tmpDirectory.mkdirs();
        }
        return tmpDirectory;
    }

    private void transferFile(HttpReader httpReader, String path, String fileName, File tmpDir)
            throws InterruptedException, IOException {
        File location = new File(tmpDir.getAbsoluteFile(), "AariHttpReader" + Math.random());

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

    private String replaceRegions(String path, String region) {
        path = path.replaceAll("\\{region\\}", region);
        return path;
    }

    private String replaceYear(String path, Integer year) {
        path = path.replaceAll("\\{yyyy\\}", year.toString());
        return path;
    }

}
