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
package dk.dma.balticweb.job;

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.log.EmbryoLogService;
import dk.dma.embryo.vessel.integration.AisVessel;
import dk.dma.embryo.vessel.service.AisDataService;
import dk.dma.embryo.vessel.service.VesselService;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Startup;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.ejb.Timeout;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.ScheduleExpression;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * AIS pull job is a experimental approach on the task of displaying AIS vessels. Its a timed job which retrieves all
 * vessels in the Baltic Sea (defined in property balticweb.vessel.aisjob.area) every x minutes (defined in balticweb.vessel.aisjob.cron)
 * The list of vessels should be stored in a in some sort of list. There is no reason to persist them.
 */
@Singleton
@Startup
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class AisPullJob {

    @Inject
    private VesselService vesselService;

    @Inject
    private AisDataService aisDataService;

    @Inject
    private EmbryoLogService embryoLogService;

    @Resource
    private TimerService service;

    @Property(value = "balticweb.vessel.aisjob.enabled")
    @Inject
    private String enabled;

    @Property(value = "balticweb.vessel.aisjob.area")
    @Inject
    private String searcharea;

    @Inject
    @Property("balticweb.vessel.aisjob.cron")
    private ScheduleExpression cron;

    @Inject
    @Property("embryo.vessel.aisjob.initialExecution")
    private int initialExecution;

    @Inject
    private Logger logger;

    public AisPullJob() {
    }

    @PostConstruct
    public void startTimer() {
        logger.info("Setting up ais pull job");

        if (enabled != null && "true".equals(enabled.trim().toLowerCase()) && cron != null) {
            // Replicate AIS information 5 seconds after startup
            if (initialExecution >= 0) {
                logger.info("Initial Execution with delay of {} milliseconds", initialExecution);
                service.createSingleActionTimer(5000, new TimerConfig(null, false));
            } else {
                logger.info("no value for embryo.vessel.aisjob.initialExecution, skipping initial execution");
            }
            logger.info("Initializing {} with {}", this.getClass().getSimpleName(), cron.toString());
            // Replicate AIS data on confirmed interval
            TimerConfig config = new TimerConfig(null, false);
            service.createCalendarTimer(cron, config);

        } else {
            logger.info("Ais PULLING job not enabled");
        }
    }

    public void replicate() {
        pullingAisVesselsFromAisTrack();
    }

    /**
     * Executes on startup
     */
    @Timeout
    void pullingAisVesselsFromAisTrack() {
        try {
            logger.debug("PULLING AIS VESSELS FROM AIS TRACK");
            // searching for all ais data in a specific area i.e. "53.0|11.0|66.0|33.0"
            List<AisVessel> aisVessels = aisDataService.getAisVesselsBBOX(searcharea);
            logger.info("Ais vessels {} found. Maybe they should be stored.", aisVessels.size());
            // TODO store the list of AIS vessels in mem and in couchDB
        } catch (Throwable t) {
            logger.error("AIS PULL Error", t);
            embryoLogService.error("AIS PULL Error", t);
        }
    }


}
