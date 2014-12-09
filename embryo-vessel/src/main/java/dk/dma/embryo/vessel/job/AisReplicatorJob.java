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
import dk.dma.embryo.vessel.json.client.AisViewServiceNorwegianData;
import dk.dma.embryo.vessel.model.Vessel;
import dk.dma.embryo.vessel.persistence.VesselDao;

@Singleton
@Startup
public class AisReplicatorJob {
    @Inject
    private VesselDao vesselRepository;

    @Inject
    private AisDataService aisDataService;
    
    /*  
    @Inject
    private FullAisViewService aisView;
     */
    @Inject
    private AisViewServiceNorwegianData aisViewWithNorwegianData;

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

            // Replite AIS data on confired interval
            TimerConfig config = new TimerConfig(null, false);
            service.createCalendarTimer(cron, config);
        } else {
            logger.info("Ais replication job not enabled");
        }
    }

    public void replicate() {
        updateAis();
    }

    @Timeout
    void updateAis() {
        try {
            logger.debug("UPDATE AIS VESSEL DATA INCLUDING NORWEGIAN DATA...");

            // Get all vessels from AIS server
            List<AisViewServiceNorwegianData.Vessel> aisServerVessels = aisViewWithNorwegianData.vesselList();
            
            // Get all vessels from ArcticWeb database
            List<Vessel> articWebVesselsAsList = vesselRepository.getAll(Vessel.class);
            
            logger.debug("aisView returns " + aisServerVessels.size() + " items - " + "repository returns " + articWebVesselsAsList.size() + " items.");
            
            Map<Long, Vessel> awVesselsAsMap = this.updateArcticWebVesselInDatabase(aisServerVessels, articWebVesselsAsList);
            
            List<AisViewServiceNorwegianData.Vessel> vesselsInAisCircle = new ArrayList<AisViewServiceNorwegianData.Vessel>();
            List<AisViewServiceNorwegianData.Vessel> vesselsOnMap = new ArrayList<AisViewServiceNorwegianData.Vessel>();

            for (AisViewServiceNorwegianData.Vessel aisVessel : aisServerVessels) {
            	
            	
            	// Ignore vessel if just one of these validations fail
            	if(aisVessel.getMmsi() == null || aisVessel.getLon() == null || aisVessel.getLat() == null) {
            		continue;
            	}
            	
                double x = aisVessel.getLon();
                double y = aisVessel.getLat();

                Long mmsi = aisVessel.getMmsi();
                
                boolean isWithInAisCircle = aisDataService.isWithinAisCircle(x, y);
                if (isWithInAisCircle || awVesselsAsMap.containsKey(mmsi)) {
                    vesselsOnMap.add(aisVessel);
                }
                if (aisDataService.isWithinAisCircle(x, y)) {
                    vesselsInAisCircle.add(aisVessel);
                }
            }
            
            logger.debug("Vessels in AIS circle: " + vesselsInAisCircle.size());
            logger.debug("Vessels on Map : " + vesselsOnMap.size());
            
            aisDataService.setVesselsInAisCircle(vesselsInAisCircle);
            aisDataService.setVesselsOnMap(vesselsOnMap);

            embryoLogService.info("AIS data replicated. Vessel count: " + vesselsInAisCircle.size());
        } catch (Throwable t) {
            logger.error("AIS Replication Error", t);
            embryoLogService.error("" + t, t);
        }
    }

	private Map<Long, Vessel> updateArcticWebVesselInDatabase(
		List<AisViewServiceNorwegianData.Vessel> result,
		List<Vessel> articWebVesselsAsList) {
		
		Map<Long, Vessel> awVesselsAsMap = new HashMap<>();

		for (Vessel v : articWebVesselsAsList) {
		    awVesselsAsMap.put(v.getMmsi(), v);
		}
		
		for (AisViewServiceNorwegianData.Vessel aisVessel : result) {
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

    
    /**
     * Executes on startup
     */
    /*
    @Timeout
    void updateAis() {
        try {
            logger.debug("UPDATE AIS VESSEL DATA");

            VesselListResult result = aisView.vesselList(0);

            List<Vessel> awVesselsAsList = vesselRepository.getAll(Vessel.class);

            logger.debug("aisView returns " + result.getVesselList().getVessels().size() + " items - "
                    + "repository returns " + awVesselsAsList.size() + " items.");

            Map<Long, Vessel> awVesselsAsMap = new HashMap<>();

            for (Vessel v : awVesselsAsList) {
                awVesselsAsMap.put(v.getMmsi(), v);
            }

            for (Entry<String, String[]> aisVessel : result.getVesselList().getVessels().entrySet()) {
                Long mmsi = asLong(aisVessel.getValue()[6]);
                String name = asString(aisVessel.getValue()[7]);
                String callSign = asString(aisVessel.getValue()[8]);
                Long imo = asLong(aisVessel.getValue()[9]);

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

            List<String[]> vesselsInAisCircle = new ArrayList<>();
            List<String[]> vesselsOnMap = new ArrayList<>();

            for (String[] aisVessel : result.getVesselList().getVessels().values()) {
                double x = Double.parseDouble(aisVessel[2]);
                double y = Double.parseDouble(aisVessel[1]);

                Long mmsi = asLong(aisVessel[6]);

                boolean isWithInAisCircle = aisDataService.isWithinAisCircle(x, y);
                if (isWithInAisCircle || awVesselsAsMap.containsKey(mmsi)) {
                    vesselsOnMap.add(aisVessel);
                }
                if (aisDataService.isWithinAisCircle(x, y)) {
                    vesselsInAisCircle.add(aisVessel);
                }
            }

            logger.debug("Vessels in AIS circle: " + vesselsInAisCircle.size());
            logger.debug("Vessels on Map : " + vesselsOnMap.size());

            aisDataService.setVesselsInAisCircle(vesselsInAisCircle);
            aisDataService.setVesselsOnMap(vesselsOnMap);

            embryoLogService.info("AIS data replicated. Vessel count: " + vesselsInAisCircle.size());
        } catch (Throwable t) {
            logger.error("AIS Replication Error", t);
            embryoLogService.error("" + t, t);
        }
    }
*/
    private boolean isUpToDate(dk.dma.embryo.vessel.model.AisData aisData, String name, String callSign, Long imo) {
        return ObjectUtils.equals(aisData.getName(), name) && ObjectUtils.equals(aisData.getCallsign(), callSign)
                && ObjectUtils.equals(aisData.getImoNo(), imo);
    }
/*
    private Long asLong(String value) {
        return value == null || value.trim().length() == 0 || value.trim().toUpperCase().equals("N/A") ? null : Long
                .valueOf(value);
    }

    private String asString(String value) {
        return value == null || value.trim().length() == 0 || value.trim().toUpperCase().equals("N/A") ? null : value;
    }
    */
}
