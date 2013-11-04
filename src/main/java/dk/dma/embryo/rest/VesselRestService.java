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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.jboss.resteasy.annotations.GZIP;
import org.slf4j.Logger;

import dk.dma.arcticweb.dao.VesselDao;
import dk.dma.arcticweb.service.ScheduleService;
import dk.dma.arcticweb.service.VesselService;
import dk.dma.embryo.domain.Route;
import dk.dma.embryo.domain.Vessel;
import dk.dma.embryo.rest.json.VesselDetails;
import dk.dma.embryo.rest.json.VesselDetails.AdditionalInformation;
import dk.dma.embryo.rest.json.VesselOverview;
import dk.dma.embryo.restclients.AisViewService;

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
        return ((Map)result.get("pastTrack")).get("points");
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

            vo.setId(Integer.parseInt(id));
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

        List<Long> mmsis = new ArrayList<>();

        for (VesselOverview vo : result) {
            mmsis.add(vo.getMmsi());
        }

        for (Vessel v : vesselDao.getVessels(mmsis).values()) {
            for (VesselOverview vo : result) {
                if (vo.getMmsi().equals(v.getMmsi())) {
                    vo.setInArcticWeb(true);
                }
            }
        }

        return result;
    }

    /**
     * Returns vessel details based ArcticWeb data and AIS data.
     */
    /*
    @GET
    @Path("/details-short")
    @Produces("application/json")
    @GZIP
    public VesselDetails detailsShort(@QueryParam("maritimeId") String maritimeId) {
        VesselDetails details = null;
        Vessel vessel = vesselService.getVessel(maritimeId);

        if (vessel != null) {
            details = vessel.toJsonModel2();
        }
        return details;
    }
    */

    @GET
    @Path("/details")
    @Produces("application/json")
    @GZIP
    public VesselDetails detailsFull(@QueryParam("mmsi") long mmsi) {
        //TODO change to execute the two calls asynchronously
        Map result = aisViewService.vesselTargetDetails(mmsi, 1);

        boolean historicalTrack = false;

        Object track = result.remove("pastTrack");

        if (track != null) {
            historicalTrack = ((List)((Map)track).get("points")).size() > 3;
        }

        VesselDetails details;
        Vessel vessel = vesselService.getVessel(mmsi);

        Route route = null;

        if (vessel != null) {
            route = scheduleService.getActiveRoute(mmsi);
            details = vessel.toJsonModel2();
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
        vesselService.save(Vessel.toJsonModel2(details));
    }

    @GET
    @Path("/details-old")
    @Produces("application/json")
    @GZIP
    public Map details(@QueryParam("id") long vesselId, @QueryParam("past_track") int pastTrack) {
        Map result = aisViewService.vesselTargetDetails(vesselId, pastTrack);
        Route route = scheduleService.getActiveRoute((long) (Integer) result.get("mmsi"));
        if (route != null) {
            result.put("route", route.toJsonModel());
        }
        return result;
    }
}
