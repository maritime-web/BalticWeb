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

import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.common.util.NamedtimeStamps;
import dk.dma.embryo.dataformats.model.ShapeFileMeasurement;
import dk.dma.embryo.dataformats.persistence.ShapeFileMeasurementDao;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.ScheduleExpression;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public abstract class AbstractJob {

    private final Logger logger = LoggerFactory.getLogger(AbstractJob.class);
    
    @Resource
    private TimerService timerService;
    
    @Inject
    protected PropertyFileService propertyFileService;
    
    @Inject
    protected ShapeFileMeasurementDao shapeFileMeasurementDao;
    
    protected NamedtimeStamps notifications = new NamedtimeStamps();
    
    protected FTPClient ftpClient; 
    protected Future<Counts> futureTransfers;
    
    protected abstract String getLocalDir(String chartType);
    protected abstract Map<String, String> getRegions(String chartType);
    protected abstract Map<String, String> getCharttypes();
    protected abstract List<ShapeFileMeasurement> getMeasurementsFromDatabase();
    
    protected static enum Dirtype {
        DIR("dir"), FILE("file");

        protected String type;

        private Dirtype(String type) {
            this.type = type;
        }
    }
    
    protected void init(String ftpServerName, ScheduleExpression cron) {
        
        if (!ftpServerName.trim().equals("") && (cron != null)) {
            logger.info("Initializing {} with {}", this.getClass().getSimpleName(), cron.toString());
            String[] localDirs = new String[getCharttypes().size()];
            List<String> regions = new ArrayList<>();
            int count = 0;
            for (String chartType : getCharttypes().values()) {
                localDirs[count] = getLocalDir(chartType);
                Map<String, String> regionsForChartType = getRegions(chartType);
                regions.addAll(regionsForChartType.keySet());
            }
            logger.info("Initializing {} with localDirectories {} and regions {}", this.getClass().getSimpleName(), localDirs, regions);
            timerService.createCalendarTimer(cron, new TimerConfig(null, false));
        } else {
            logger.info("FTP site is not configured - cron job not scheduled.");
        }
    }

    protected void shutdown() {
        logger.info("Shutdown called.");
        if(this.ftpClient != null) {

            boolean logout = false;
            try {
                logout = this.ftpClient.logout();
            } catch (IOException e) {
                logger.error("Could not close FTP Connection", e);
            }
            logger.info("did the job manage to logout -> " + logout);
        }
        
        if(this.futureTransfers != null) {
            boolean cancelSucceeded = this.futureTransfers.cancel(true);
            logger.info("did the job manage to cancel -> " + cancelSucceeded);
        }
    }
    
    protected void makeDirectoriesIfNecessary() {
        logger.info("Making directories if necessary ...");
        for (String chartType : getCharttypes().values()) {
            String localDir = getLocalDir(chartType);
            if (!new File(localDir).exists()) {
                logger.info("Making local directory for files: " + localDir);
                new File(localDir).mkdirs();
            }
        }
    }
}
