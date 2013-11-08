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
package dk.dma.embryo.rest;

import dk.dma.arcticweb.dao.VesselDao;
import dk.dma.arcticweb.service.ScheduleService;
import dk.dma.arcticweb.service.VesselService;
import dk.dma.embryo.domain.Route;
import dk.dma.embryo.domain.Vessel;
import dk.dma.embryo.rest.json.VesselDetails;
import dk.dma.embryo.rest.json.VesselDetails.AdditionalInformation;
import dk.dma.embryo.rest.json.VesselOverview;
import dk.dma.embryo.restclients.AisViewService;
import org.jboss.resteasy.annotations.GZIP;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/vessel")
public class VesselRestService {
    @Inject
    private AisViewService aisViewService;

    @Inject
    private Logger logger;

    @Inject
    private VesselService vesselService;

    @Inject
    private ScheduleService scheduleService;

    @Inject
    private VesselDao vesselDao;

    @GET
    @Path("/historical-track")
    @Produces("application/json")
    @GZIP
    public Object historicalTrack(@QueryParam("mmsi") long mmsi) {
        Map result = aisViewService.vesselTargetDetails(mmsi, 1);
        return ((Map) result.get("pastTrack")).get("points");
    }

    @GET
    @Path("/list")
    @Produces("application/json")
    @GZIP
    public List<VesselOverview> list() {
        AisViewService.VesselListResult vesselListResult = aisViewService.vesselList(0);

        List<VesselOverview> result = new ArrayList<>();

        Map<String, String[]> vessels = vesselListResult.getVesselList().getVessels();

        for (String id : vessels.keySet()) {
            String[] vessel = vessels.get(id);

            VesselOverview vo = new VesselOverview();

            vo.setX(Double.parseDouble(vessel[2]));
            vo.setY(Double.parseDouble(vessel[1]));
            vo.setAngle(Double.parseDouble(vessel[0]));
            vo.setMmsi(Long.parseLong(vessel[6]));
            vo.setName(vessel[7]);
            vo.setImo(vessel[9]);
            vo.setCallSign(vessel[8]);
            vo.setMoored("1".equals(vessel[5]));
            vo.setType(vessel[4]);
            vo.setInArcticWeb(false);

            // What is vessel[3] seems to be either A or B ?

            result.add(vo);
        }

        Map<Long, VesselOverview> resultAsMap = new HashMap<>();

        for (VesselOverview vo : result) {
            resultAsMap.put(vo.getMmsi(), vo);
        }

        List<Vessel> allArcticWebVessels = vesselDao.getAll(Vessel.class);

        for (Vessel v : allArcticWebVessels) {
            VesselOverview vesselOverview = resultAsMap.get(v.getMmsi());

            if (vesselOverview != null) {
                vesselOverview.setInArcticWeb(true);
            } else {
                VesselOverview vo = new VesselOverview();
                vo.setInArcticWeb(true);
                vo.setCallSign(v.getAisData().getCallsign());
                vo.setName(v.getAisData().getName());
                vo.setImo("" + v.getAisData().getImoNo());
                vo.setMmsi(v.getMmsi());
                result.add(vo);
            }
        }

        return result;
    }

    @GET
    @Path("/details")
    @Produces("application/json")
    @GZIP
    public VesselDetails details(@QueryParam("mmsi") long mmsi) {
        //
        Map result = null;
        try{
            result = aisViewService.vesselTargetDetails(mmsi, 1);
        }catch(Exception e){
            // Make sure ArcticWeb reporting can be used even though AIS server is not present
            logger.error("Error when calling ais server", e);
            result = new HashMap<>();
        }

        boolean historicalTrack = false;

        Object track = result.remove("pastTrack");

        if (track != null) {
            historicalTrack = ((List) ((Map) track).get("points")).size() > 3;
        }

        VesselDetails details;
        Vessel vessel = vesselService.getVessel(mmsi);

        Route route = null;

        if (vessel != null) {
            route = scheduleService.getActiveRoute(mmsi);
            details = vessel.toJsonModel();
            details.getAis().putAll(result);
        } else {
            details = new VesselDetails();
            details.setAis(result);
        }

        details.setAdditionalInformation(
                new AdditionalInformation(
                        route != null ? route.getEnavId() : null, historicalTrack
                )
        );

        return details;
    }

    @POST
    @Path("/save-details")
    @Consumes("application/json")
    @GZIP
    public void saveDetails(VesselDetails details) {
        logger.info("save({})", details);
        vesselService.save(Vessel.fromJsonModel(details));
    }
}
