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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.log.EmbryoLogService;
import dk.dma.embryo.vessel.json.client.AisViewServiceAllAisData;
import dk.dma.embryo.vessel.model.Vessel;
import dk.dma.embryo.vessel.persistence.VesselDao;

@Singleton
@Startup
public class AisReplicatorJob {
    @Inject
    private VesselDao vesselRepository;

    @Inject
    private AisDataService aisDataService;

    @Inject
    private AisViewServiceAllAisData aisViewWithNorwegianData;

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

    @Inject
    private EmbryoLogService embryoLogService;

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
        updateAis();
    }

    /**
     * Executes on startup
     */
    @Timeout
    void updateAis() {
        try {
            logger.info("UPDATE AIS VESSEL DATA INCLUDING NORWEGIAN DATA...");

            // Get all vessels from AIS server
            List<AisViewServiceAllAisData.Vessel> aisServerAllVessels = aisViewWithNorwegianData.vesselList(AisViewServiceAllAisData.LOOK_BACK_PT24H, AisViewServiceAllAisData.LOOK_BACK_PT24H);
            
            // Get all vessels from ArcticWeb database
            List<Vessel> articWebVesselsAsList = vesselRepository.getAll(Vessel.class);

            logger.info("aisView returns " + aisServerAllVessels.size() + " items - " + "repository returns " + articWebVesselsAsList.size() + " items.");

            Map<Long, Vessel> awVesselsAsMap = this.updateArcticWebVesselInDatabase(aisServerAllVessels, articWebVesselsAsList);

            List<AisViewServiceAllAisData.Vessel> vesselsInAisCircle = new ArrayList<AisViewServiceAllAisData.Vessel>();
            List<AisViewServiceAllAisData.Vessel> vesselsOnMap = new ArrayList<AisViewServiceAllAisData.Vessel>();
            List<AisViewServiceAllAisData.Vessel> vesselsAllowed = new ArrayList<AisViewServiceAllAisData.Vessel>();

            for (AisViewServiceAllAisData.Vessel aisVessel : aisServerAllVessels) {

                // Ignore vessel if just one of these validations fail
                if(aisVessel.getMmsi() == null || aisVessel.getLon() == null || aisVessel.getLat() == null) {
                    continue;
                }

                double longitude = aisVessel.getLon();
                double latitude = aisVessel.getLat();

                Long mmsi = aisVessel.getMmsi();

                boolean isAllowed = aisDataService.isAllowed(latitude);
                boolean isWithInAisCircle = aisDataService.isWithinAisCircle(longitude, latitude);
                
                /*
                 * These vessels are used by the VesselRestService to match against selection groups.
                 */
                if (isAllowed || awVesselsAsMap.containsKey(mmsi)) {
                    vesselsAllowed.add(aisVessel);
                }
                
                /*
                 * These vessels are shown on the map if logged on user has no selection groups and 
                 * contains vessels within plus Arctic Web vessels even if they are outside the default circle and is allowed.
                 */
                if ( awVesselsAsMap.containsKey(mmsi) || (isAllowed && isWithInAisCircle) ) {
                    vesselsOnMap.add(aisVessel);
                }
                
                /*
                 * These vessels are used by MaxSpeadJob.java and 
                 * do not contain Arctic Web vessels from the database if they are outside the default circle and is allowed.
                 */
                if (isAllowed && aisDataService.isWithinAisCircle(longitude, latitude)) {
                    vesselsInAisCircle.add(aisVessel);
                }
            }

            logger.info("Vessels in AIS circle: " + vesselsInAisCircle.size());
            logger.info("Vessels on Map : " + vesselsOnMap.size());
            logger.info("Vessels allowed : " + vesselsAllowed.size());
            
            aisDataService.setVesselsAllowed(vesselsAllowed);
            aisDataService.setVesselsInAisCircle(vesselsInAisCircle);
            aisDataService.setVesselsOnMap(vesselsOnMap);

            embryoLogService.info("AIS data replicated. Vessel count: " + vesselsInAisCircle.size());
        } catch (Throwable t) {
            logger.error("AIS Replication Error", t);
            embryoLogService.error("" + t, t);
        }
    }

    private Map<Long, Vessel> updateArcticWebVesselInDatabase(
            List<AisViewServiceAllAisData.Vessel> result,
            List<Vessel> articWebVesselsAsList) {

        Map<Long, Vessel> awVesselsAsMap = new HashMap<>();

        for (Vessel v : articWebVesselsAsList) {
            awVesselsAsMap.put(v.getMmsi(), v);
        }

        for (AisViewServiceAllAisData.Vessel aisVessel : result) {
            Long mmsi = aisVessel.getMmsi();
            String name = aisVessel.getName();
            String callSign = aisVessel.getCallsign();
            Long imo = aisVessel.getImoNo();

            if (mmsi != null) {
                Vessel vessel = awVesselsAsMap.get(mmsi);

                if (vessel != null && name != null && callSign != null) {
                    if (!isUpToDate(vessel.getAisData(), name, callSign, imo)) {
                        vessel.getAisData().setCallsign(callSign);
                        vessel.getAisData().setImoNo(imo);
                        vessel.getAisData().setName(name);
                        logger.debug("Updating vessel {}/{}", mmsi, name);
                        vesselRepository.saveEntity(vessel);
                    } else {
                        logger.debug("Vessel {}/{} is up to date", mmsi, name);
                    }
                }
            }
        }

        return awVesselsAsMap;
    }
    
    private boolean isUpToDate(dk.dma.embryo.vessel.model.AisData aisData, String name, String callSign, Long imo) {
        return ObjectUtils.equals(aisData.getName(), name) && ObjectUtils.equals(aisData.getCallsign(), callSign)
                && ObjectUtils.equals(aisData.getImoNo(), imo);
    }
}
