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
package dk.dma.embryo.vessel.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import com.google.common.collect.Collections2;

import dk.dma.embryo.vessel.job.AisDataService;
import dk.dma.embryo.vessel.job.AisReplicatorJob;
import dk.dma.embryo.vessel.job.MaxSpeedJob;
import dk.dma.embryo.vessel.job.MaxSpeedJob.MaxSpeedRecording;
import dk.dma.embryo.vessel.job.ShipTypeCargo.ShipType;
import dk.dma.embryo.vessel.job.ShipTypeMapper;
import dk.dma.embryo.vessel.job.filter.UserSelectionGroupsFilter;
import dk.dma.embryo.vessel.json.client.AisViewServiceAllAisData;
import dk.dma.embryo.vessel.json.client.LimitedAisViewService;
import dk.dma.embryo.vessel.model.Route;
import dk.dma.embryo.vessel.model.Vessel;
import dk.dma.embryo.vessel.model.Voyage;
import dk.dma.embryo.vessel.persistence.VesselDao;
import dk.dma.embryo.vessel.service.ScheduleService;
import dk.dma.embryo.vessel.service.VesselService;

@Path("/vessel")
@RequestScoped
public class VesselRestService {
    @Inject
    private LimitedAisViewService limitedAisViewService;

    @Inject
    private AisDataService aisDataService;

    @Inject
    private Logger logger;

    @Inject
    private VesselService vesselService;

    @Inject
    private ScheduleService scheduleService;

    @Inject
    private VesselDao vesselDao;

    @Inject
    private MaxSpeedJob maxSpeedJob;

    @Inject
    private AisReplicatorJob aisReplicatorJob;

    @Inject
    private UserSelectionGroupsFilter userSelectionGroupsFilter;

    @GET
    @Path("/historical-track")
    @Produces("application/json")
    @GZIP
    @NoCache
    public Object historicalTrack(@QueryParam("mmsi") long mmsi) {
        Map<String, Object> result = limitedAisViewService.vesselTargetDetails(mmsi, 1);
        return ((Map) result.get("pastTrack")).get("points");
    }

    @GET
    @Path("/list")
    @Produces("application/json")
    @GZIP
    @NoCache
    public List<VesselOverview> list() {

        List<VesselOverview> allAllowedAisVesselsAsDTO = this.mapAisVessels(aisDataService.getVesselsAllowed());
        Map<Long, VesselOverview> allAllowedAisVesselsAsMap = mapifyResult(allAllowedAisVesselsAsDTO);

        List<VesselOverview> result = new ArrayList<VesselOverview>();
        if (this.userSelectionGroupsFilter.loggedOnUserHasSelectionGroups()) {
            result.addAll(Collections2.filter(allAllowedAisVesselsAsDTO, userSelectionGroupsFilter));
        } else {
            // Go default
            result = this.mapAisVessels(aisDataService.getVesselsOnMap());
        }
        // ArcticWeb vessels from the database are always shown on the map
        List<Vessel> allArcticWebVessels = vesselDao.getAll(Vessel.class);
        for (Vessel vesselFromDatabase : allArcticWebVessels) {
            VesselOverview aisVesselOverview = allAllowedAisVesselsAsMap.get(vesselFromDatabase.getMmsi());

            if (alsoArcticWebVessel(aisVesselOverview)) {
                aisVesselOverview.setInAW(true);
                result.add(aisVesselOverview);
            } else {
                VesselOverview arcticWebVesselOnly = createVesselOverview(vesselFromDatabase);
                result.add(arcticWebVesselOnly);
            }
        }
        return result;
    }

    private VesselOverview createVesselOverview(Vessel vesselFromDatabase) {
        VesselOverview arcticWebVesselOnly = new VesselOverview();
        arcticWebVesselOnly.setInAW(true);
        arcticWebVesselOnly.setCallSign(vesselFromDatabase.getAisData().getCallsign());
        arcticWebVesselOnly.setName(vesselFromDatabase.getAisData().getName());
        arcticWebVesselOnly.setMmsi(vesselFromDatabase.getMmsi());
        return arcticWebVesselOnly;
    }

    private boolean alsoArcticWebVessel(VesselOverview vesselOverview) {
        return vesselOverview != null;
    }

    private Map<Long, VesselOverview> mapifyResult(List<VesselOverview> result) {
        Map<Long, VesselOverview> resultAsMap = new HashMap<>();
        for (VesselOverview vo : result) {
            resultAsMap.put(vo.getMmsi(), vo);
        }
        return resultAsMap;
    }

    private List<VesselOverview> mapAisVessels(List<AisViewServiceAllAisData.Vessel> vessels) {

        List<AisViewServiceAllAisData.Vessel> allowedVessels = vessels;
        Map<Long, MaxSpeedRecording> speeds = aisDataService.getMaxSpeeds();

        List<VesselOverview> vesselOverviewsResponse = new ArrayList<VesselOverview>();

        for (AisViewServiceAllAisData.Vessel vessel : allowedVessels) {

            VesselOverview vesselOverview = new VesselOverview();
            Long mmsi = vessel.getMmsi();

            vesselOverview.setX(vessel.getLon());
            vesselOverview.setY(vessel.getLat());
            vesselOverview.setAngle(vessel.getCog() != null ? vessel.getCog() : 0);
            vesselOverview.setMmsi(mmsi);
            vesselOverview.setName(vessel.getName());
            vesselOverview.setCallSign(vessel.getCallsign());
            vesselOverview.setMoored(vessel.getMoored() != null ? vessel.getMoored() : false);

            ShipType shipTypeFromSubType = ShipType.getShipTypeFromSubType(vessel.getVesselType());
            String type = ShipTypeMapper.getInstance().getColor(shipTypeFromSubType).ordinal() + "";
            vesselOverview.setType(type);

            vesselOverview.setInAW(false);

            // What is vessel[3] seems to be either A or B ?

            MaxSpeedRecording speed = speeds.get(mmsi);
            vesselOverview.setMsog(speed != null ? speed.getMaxSpeed() : 0.0);

            vesselOverviewsResponse.add(vesselOverview);
        }

        return vesselOverviewsResponse;
    }

    @GET
    @Path("/details")
    @Produces("application/json")
    @GZIP
    @NoCache
    @Details
    public VesselDetails details(@QueryParam("mmsi") long mmsi) {
        logger.debug("details({})", mmsi);

        try {

            logger.info("MMSI -> " + mmsi);
            dk.dma.embryo.vessel.json.client.AisViewServiceAllAisData.Vessel aisVessel = this.aisDataService.getAisVesselByMmsi(mmsi);

            boolean historicalTrack = false;
            Double lat = null;
            Double lon = null;

            if (aisVessel != null) {
                lat = aisVessel.getLat();
                lon = aisVessel.getLon();
            }

            if (lat != null && lon != null) {
                historicalTrack = aisDataService.isAllowed(lat);
            }

            VesselDetails details;
            Vessel vessel = vesselService.getVessel(mmsi);
            List<Voyage> schedule = null;
            Route route = null;

            if (vessel != null) {
                route = scheduleService.getActiveRoute(mmsi);
                details = vessel.toJsonModel();
                schedule = scheduleService.getSchedule(mmsi);
                details.setAisVessel(aisVessel);
            } else {
                details = new VesselDetails();
                details.setAisVessel(aisVessel);
            }

            Map<String, Object> additionalInformation = new HashMap<>();
            additionalInformation.put("historicalTrack", historicalTrack);
            additionalInformation.put("routeId", route != null ? route.getEnavId() : null);
            additionalInformation.put("schedule", schedule != null && schedule.size() > 0);

            details.setAdditionalInformation(additionalInformation);

            return details;

        } catch (Throwable t) {

            logger.info("Ignoring exception " + t, t);

            // fallback on database only

            Vessel vessel = vesselService.getVessel(mmsi);

            if (vessel != null) {
                VesselDetails details;
                Route route = scheduleService.getActiveRoute(mmsi);
                List<Voyage> schedule = scheduleService.getSchedule(mmsi);

                details = vessel.toJsonModel();

                AisViewServiceAllAisData.Vessel aisVessel = new AisViewServiceAllAisData.Vessel();
                aisVessel.setCallsign(vessel.getAisData().getCallsign());
                aisVessel.setImoNo(vessel.getAisData().getImoNo());
                aisVessel.setMmsi(vessel.getMmsi());
                aisVessel.setName(vessel.getAisData().getName());

                Map<String, Object> additionalInformation = new HashMap<>();
                additionalInformation.put("routeId", route != null ? route.getEnavId() : null);
                additionalInformation.put("historicalTrack", false);
                additionalInformation.put("schedule", schedule != null && schedule.size() > 0);
                details.setAdditionalInformation(additionalInformation);

                return details;
            } else {
                throw new RuntimeException("No vessel details available for " + mmsi + " caused by " + t);
            }
        }
    }

    @POST
    @Path("/save-details")
    @Consumes("application/json")
    @GZIP
    public void saveDetails(VesselDetails details) {
        logger.debug("save({})", details);
        vesselService.save(Vessel.fromJsonModel(details));
    }

    @PUT
    @Path("/update/ais")
    @Consumes("application/json")
    public void updateAis() {
        logger.debug("updateAis()");
        aisReplicatorJob.replicate();
    }

    @PUT
    @Path("/update/maxspeeds")
    @Consumes("application/json")
    public void updateMaxSpeeds() {
        logger.debug("updateMaxSpeeds()");
        maxSpeedJob.update();
    }
}
