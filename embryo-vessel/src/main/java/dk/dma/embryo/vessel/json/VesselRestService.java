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

import dk.dma.embryo.common.EmbryonicException;
import dk.dma.embryo.common.json.AbstractRestService;
import dk.dma.embryo.vessel.integration.AisVessel;
import dk.dma.embryo.vessel.job.AisReplicatorJob;
import dk.dma.embryo.vessel.model.Route;
import dk.dma.embryo.vessel.model.Vessel;
import dk.dma.embryo.vessel.model.Voyage;
import dk.dma.embryo.vessel.service.AisDataService;
import dk.dma.embryo.vessel.service.ScheduleService;
import dk.dma.embryo.vessel.service.VesselService;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.client.ClientResponseFailure;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/vessel")
@RequestScoped
public class VesselRestService extends AbstractRestService {

    @Inject
    private AisDataService aisDataService;

    @Inject
    private Logger logger;

    @Inject
    private VesselService vesselService;

    @Inject
    private ScheduleService scheduleService;

    @Inject
    private AisReplicatorJob aisReplicatorJob;

    @GET
    @Path("/historical-track")
    @Produces("application/json")
    @GZIP
    public Response historicalTrack(@Context Request request, @QueryParam("mmsi") long mmsi) {

        List<TrackPos> historicalTrack = null;
        
        // Call long track if this call times out or get any kind of error call the regular track.
        /*
        try {
            
            historicalTrack = historicalLongTrackWithTimeout(mmsi);
            logger.info("Historical LONG track called with success.");
        } catch (Exception e) {

            historicalTrack = this.historicalTrackAisViewService.historicalTrack(mmsi, 500, AisViewServiceAllAisData.LOOK_BACK_PT24H);
            logger.info("Historical LONG track timeout or failed but SHORT track called instead with success.");
        } 
        */
        
        // The above statments are kept as comments because LONG tracks are disable because of instability.
        
        try {
            //historicalTrack = aisDataService.historicalTrack(mmsi, 500, );
            historicalTrack = aisDataService.historicalTrack(mmsi);
        } catch (ClientResponseFailure crf) {

            if(crf.getResponse().getStatus() == 404) {
                return Response.noContent().build();
            } else {
                throw crf;
            }
        }

        return super.getResponse(request, historicalTrack, MAX_AGE_10_MINUTES);
        
    }
    /*
    private List<TrackSingleLocation> historicalLongTrackWithTimeout(long mmsi) throws IOException {
        
        HttpParams httpParams = new BasicHttpParams();
        HttpClient httpClient = new DefaultHttpClient();

        // Determines the timeout until a connection is etablished.
        HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
        // Defines the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
        HttpConnectionParams.setSoTimeout(httpParams, 10000);

        
        String url = this.fullAisViewServiceInclNorwegianDataUrl;

        HttpGet getRequest = new HttpGet(url + "/vessel/longtrack/" + mmsi);
        getRequest.addHeader("accept", "application/json");

        httpParams.setIntParameter("minDist", 500);
        httpParams.setParameter("age", AisViewServiceAllAisData.LOOK_BACK_PT120H);
        getRequest.setParams(httpParams);

        HttpResponse response = httpClient.execute(getRequest);

        String json = EntityUtils.toString(response.getEntity());

        List<LinkedHashMap<String, Object>> ob = new ObjectMapper().readValue(json, ArrayList.class);

        List<TrackSingleLocation> historicalTrack = new ArrayList<>();
        for (LinkedHashMap<String, Object> linkedHashMap : ob) {
            Double cog = (Double) linkedHashMap.get("cog");
            Double lat = (Double) linkedHashMap.get("lat");
            Double lon = (Double) linkedHashMap.get("lon");
            Double sog = (Double) linkedHashMap.get("sog");
            Long time = (Long) linkedHashMap.get("time");

            TrackSingleLocation point = new TrackSingleLocation(cog, lat, lon, sog, time);
            historicalTrack.add(point);
        }

        httpClient.getConnectionManager().shutdown();

        return historicalTrack;
    }*/

    @GET
    @Path("/list")
    @Produces("application/json")
    @GZIP
    public Response list(@Context Request request) {
        List<AisVessel> aisVessels = this.aisDataService.getAisVessels();

        List<Vessel> vessels = vesselService.getAll();
        aisVessels = AisVessel.addMissingVessels(aisVessels, vessels);

        final Map<Long, Vessel> arcticWebVessels = Vessel.asMap(vessels);
        List<VesselOverview> result = AisVessel.toVesselOverviewStream(aisVessels).map(
                vessel -> {
                    vessel.setInAW(arcticWebVessels.containsKey(vessel.getMmsi()));
                    return vessel;
                }).collect(Collectors.toList());

        return super.getResponse(request, result, NO_CACHE);
    }

    @GET
    @Path("/details")
    @Produces("application/json")
    @GZIP
    @Details
    public Response details(@Context Request request, @QueryParam("mmsi") long mmsi) {
        logger.debug("details({})", mmsi);

        VesselDetails details;
        try {
            AisVessel aisVessel = this.aisDataService.getAisVesselByMmsi(mmsi);
            Vessel vessel = vesselService.getVessel(mmsi);
            List<Voyage> schedule = null;
            Route route = null;
            if (vessel != null) {
                details = vessel.toJsonModel();
                route = scheduleService.getActiveRoute(mmsi);
                schedule = scheduleService.getSchedule(mmsi);
            } else {
                details = new VesselDetails();
            }
            details.setAisVessel(aisVessel);

            AdditionalInformationBuilder builder = new AdditionalInformationBuilder();
            builder.addHistoricalTrackInformation(aisDataService.isHistoricalTrackAllowed(aisVessel));
            builder.addRouteInformation(route).addSchedule(schedule);
            details.setAdditionalInformation(builder.build());

        } catch (Exception e) {
            Vessel vessel = vesselService.getVessel(mmsi);

            if (vessel != null) {
                logger.warn("Ignoring caught exception. Fallback to database only", e);
                details = vessel.toJsonModel();
                details.setAisVessel(AisVessel.create(vessel));

                Route route = scheduleService.getActiveRoute(mmsi);
                List<Voyage> schedule = scheduleService.getSchedule(mmsi);

                AdditionalInformationBuilder builder = new AdditionalInformationBuilder();
                builder.addHistoricalTrackInformation(false);
                builder.addRouteInformation(route).addSchedule(schedule);
                details.setAdditionalInformation(builder.build());
            } else {
                throw new EmbryonicException("No vessel details available for " + mmsi, e);
            }
        }

        logger.debug("details({}) : {}", details);
        Response response = super.getResponse(request, details, MAX_AGE_10_MINUTES);
        return response;
    }

    @POST
    @Path("/save-details")
    @Consumes("application/json")
    @GZIP
    public void saveDetails(VesselDetails details) {
        logger.debug("save({})", details);
        vesselService.save(dk.dma.embryo.vessel.model.Vessel.fromJsonModel(details));
    }

    @PUT
    @Path("/update/ais")
    @Consumes("application/json")
    public void updateAis() {
        logger.debug("updateAis()");
        aisReplicatorJob.replicate();
    }

    public static class AdditionalInformationBuilder {
        Map<String, Object> additionalInformation = new HashMap<>();

        public AdditionalInformationBuilder addHistoricalTrackInformation(boolean historicalTrackAllowed){
            additionalInformation.put("historicalTrack", historicalTrackAllowed);
            return this;
        }

        public AdditionalInformationBuilder addRouteInformation(Route route){
            additionalInformation.put("routeId", route != null ? route.getEnavId() : null);
            return this;
        }

        public AdditionalInformationBuilder addSchedule(List<Voyage> schedule){
            additionalInformation.put("schedule", schedule != null && schedule.size() > 0);
            return this;
        }


        public Map<String, Object> build(){
            return additionalInformation;
        }
    }

}
