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

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.log.EmbryoLogService;
import dk.dma.embryo.dataformats.job.JobContext.Context;
import dk.dma.embryo.dataformats.model.ShapeFileMeasurement;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Singleton
@Startup
public class DmiFtpReaderJob extends AbstractJob {

    private final Logger logger = LoggerFactory.getLogger(DmiFtpReaderJob.class);

    @Inject
    @Property("embryo.iceChart.dmi.cron")
    private ScheduleExpression cron;

    @Inject
    @Property("embryo.iceChart.dmi.ftp.baseDirectory")
    private String dmiBaseDirectory;

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
    @Property("embryo.ice.charttypes")
    private Map<String, String> charttypes;

    @Inject
    @Property("embryo.ftp.dirtypes")
    private Map<String, String> dirtypes;

    @Inject
    @Property("embryo.iceChart.dmi.notification.silenceperiod")
    private Integer silencePeriod;
   
    @Inject
    @Property("embryo.iceChart.dmi.notification.email")
    private String mailTo;

    @Inject
    @Property("embryo.iceChart.dmi.ftp.ageInDays")
    private Integer ageInDays;

    @Inject
    @Property(value = "embryo.tmpDir", substituteSystemProperties = true)
    private String tmpDir;

    @Inject
    protected EmbryoLogService embryoLogService; 
    
    @EJB
    private FtpAsyncProxy asyncProxy;
    
    @PostConstruct
    public void init() {
        
        super.init(dmiServer, cron);
    }

    @Timeout
    public void timeout() throws IOException {
        notifications.clearOldThanMinutes(silencePeriod);

        try {
            makeDirectoriesIfNecessary();
            
            logger.info("Calling transfer files ...");
            
            this.ftpClient = new FTPClient();
            logger.info("Connecting to " + dmiServer + " using " + dmiLogin + " ...");

            ftpClient.setDefaultTimeout(30000);
            ftpClient.connect(dmiServer);
            ftpClient.login(dmiLogin, dmiPassword);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            
            JobContext jobContext = JobContext.createNewInstance(
                    charttypes, 
                    dirtypes, 
                    getMeasurementsFromDatabase(), 
                    propertyFileService,
                    ftpClient, 
                    dmiBaseDirectory, 
                    "dmiFtpReader", 
                    ageInDays, 
                    mailTo, 
                    tmpDir,
                    this.embryoLogService,
                    Context.DMI);
            
            this.futureTransfers = this.asyncProxy.transferFiles(jobContext);
            
        } catch (Throwable t) {
            logger.error("Unhandled error scanning/transfering files from DMI (" + dmiServer + "): " + t, t);
            embryoLogService.error("Unhandled error scanning/transfering files from DMI (" + dmiServer + "): " + t, t);
            this.ftpClient.logout();
        }
    }

    @PreDestroy
    public void shutdown() {
        super.shutdown();
    }
    
    @Override
    protected String getLocalDir(String chartType) {
        return propertyFileService.getProperty("embryo." + chartType + ".dmi.localDirectory", true);
    }

    @Override
    protected Map<String, String> getRegions(String chartType) {
        return propertyFileService.getMapProperty("embryo." + chartType + ".dmi.regions");
    }
    
    @Override
    protected Map<String, String> getCharttypes() {
        return charttypes;
    }

    @Override
    protected List<ShapeFileMeasurement> getMeasurementsFromDatabase() {
        return this.shapeFileMeasurementDao.list("iceChart", "dmi");
    }
}
