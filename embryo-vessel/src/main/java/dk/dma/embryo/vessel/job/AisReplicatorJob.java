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
package dk.dma.embryo.vessel.job;

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.log.EmbryoLogService;
import dk.dma.embryo.vessel.integration.AisVessel;
import dk.dma.embryo.vessel.model.AisData;
import dk.dma.embryo.vessel.model.Vessel;
import dk.dma.embryo.vessel.service.AisDataService;
import dk.dma.embryo.vessel.service.VesselService;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
@Startup
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class AisReplicatorJob {
    
    @Inject
    private VesselService vesselService;

    @Inject
    private AisDataService aisDataService;

    @Inject
    private EmbryoLogService embryoLogService;

    @Resource
    private TimerService service;

    @Property(value = "embryo.vessel.aisjob.enabled")
    @Inject
    private String enabled;
    
    @Inject
    @Property("embryo.vessel.aisjob.cron")
    private ScheduleExpression cron;

    @Inject
    @Property("embryo.vessel.aisjob.initialExecution")
    private int initialExecution;

    @Inject
    private Logger logger;

    public AisReplicatorJob() {
    }

    @PostConstruct
    public void startTimer() {
        logger.info("Setting up ais replication job");

        if (enabled != null && "true".equals(enabled.trim().toLowerCase()) && cron != null) {
            // Replicate AIS information 5 seconds after startup
            if (initialExecution >= 0) {
                logger.info("Initial Execution with delay of {} milliseconds", initialExecution);
                service.createSingleActionTimer(5000, new TimerConfig(null, false));
            } else {
                logger.info("no value for embryo.vessel.aisjob.initialExecution, skipping initial execution");
            }

            // Replicate AIS data on confired interval
            TimerConfig config = new TimerConfig(null, false);
            service.createCalendarTimer(cron, config);
        } else {
            logger.info("Ais replication job not enabled");
        }
    }

    public void replicate() {
        updateAisBaseData();
    }

    /**
     * Executes on startup
     */
    @Timeout
    void updateAisBaseData() {
        try {
            logger.info("UPDATING AIS BASE DATA ON VESSELS");

            List<Vessel> arcticWebVessels = vesselService.getAll();

            List<AisVessel> aisVessels = aisDataService.getAisVesselsByMmsi(Vessel.extractMmsiNumbers(arcticWebVessels));

            final Map<Long, AisVessel> aisVesselsByMmsi = AisVessel.asMap(aisVessels);
            List<Vessel> vesselsToUpdate = new VesselsToUpdateBuilder().setAWVessels(arcticWebVessels).setAisVessels(aisVesselsByMmsi).build();
            List<Vessel> failedVessels = new LinkedList<>();
            for(Vessel vessel : vesselsToUpdate){
                try{
                    vesselService.save(vessel);

                    logger.info("AIS base data updated, mmsi={}, name='{}', callSign='{}', imo='{}'",
                            vessel.getMmsi(), vessel.getAisData().getName(), vessel.getAisData().getCallsign(), vessel.getAisData().getImoNo() );
                }catch(Exception e){
                    failedVessels.add(vessel);
                    String msg = "Failed updating AIS base data on ArcticWeb vessel with mmsi=" + vessel.getMmsi();
                    logger.error(msg, e);
                    embryoLogService.error(msg, e);
                }
            }

            if(failedVessels.size() == 0){
                String message = "Updated AIS data for " + vesselsToUpdate.size() + " of " + arcticWebVessels.size()+ " ArcticWeb vessels";
                logger.info(message);
                embryoLogService.info(message);
            }else{
                String msg = "AIS Replication Error. Failed saving data for " + failedVessels.size() + "vessels";
                logger.error(msg);
                embryoLogService.error(msg);
            }
        } catch (Throwable t) {
            logger.error("AIS Replication Error", t);
            embryoLogService.error("AIS Replication Error", t);
        }
    }

    public static class VesselsToUpdateBuilder {
        private Map<Long, AisVessel> aisVessels;
        private List<Vessel> arcticWebVessels;

        public VesselsToUpdateBuilder setAisVessels(Map<Long, AisVessel> aisVessels) {
            this.aisVessels = aisVessels;
            return this;
        }

        public VesselsToUpdateBuilder setAWVessels(List<Vessel> arcticWebVessels) {
            this.arcticWebVessels = arcticWebVessels;
            return this;
        }

        List<Vessel> build() {
            List<Vessel> vesselsToUpdate = arcticWebVessels.stream().filter(vessel -> {
                AisVessel ves = aisVessels.get(vessel.getMmsi());
                return ves != null && !vessel.isUpToDate(ves.getName(), ves.getCallsign(), ves.getImoNo());
            }).map(vessel -> {
                AisVessel ves = aisVessels.get(vessel.getMmsi());
                vessel.setAisData(new AisData(ves.getName(), ves.getCallsign(), ves.getImoNo()));
                return vessel;
            }).collect(Collectors.toList());
            return vesselsToUpdate;
        }
    }
}
