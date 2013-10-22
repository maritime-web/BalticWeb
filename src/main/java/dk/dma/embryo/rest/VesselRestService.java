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

import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.slf4j.Logger;

import dk.dma.arcticweb.service.ShipService;
import dk.dma.embryo.domain.Route;
import dk.dma.embryo.domain.Ship;
import dk.dma.embryo.rest.json.VesselDetails;
import dk.dma.embryo.rest.json.VesselDetails.AdditionalInformation;
import dk.dma.embryo.restclients.AisViewService;

@Path("/vessel")
public class VesselRestService {
    @Inject
    private AisViewService aisViewService;

    @Inject
    private Logger logger;

    @Inject
    private ShipService vesselService;

    @GET
    @Path("/historical-track")
    @Produces("application/json")
    public Object historicalTrack(@QueryParam("id") long vesselId) {
        Map result = aisViewService.vesselTargetDetails(vesselId, 1);
        return result.remove("pastTrack");
    }

    /**
     * Returns vessel details based ArcticWeb data and AIS data.
     * 
     * @param mmsi
     * @return
     */
    @GET
    @Path("/details-short")
    @Produces("application/json")
    public VesselDetails detailsByMmsi(@QueryParam("maritimeId") String maritimeId) {
        VesselDetails details = null;
        Ship vessel = vesselService.getVessel(maritimeId);

        if (vessel != null) {
            details = vessel.toJsonModel2();
        }
        return details;
    }

    @GET
    @Path("/details-by-ais")
    @Produces("application/json")
    public VesselDetails detailsByAis(@QueryParam("id") long vesselId) {
        Map result = aisViewService.vesselTargetDetails(vesselId, 1);

        Integer mmsiStr = (Integer) result.get("mmsi");
        Object track = result.remove("pastTrack");

        VesselDetails details = null;
        Ship vessel = null;
        Route route = null;
        if (mmsiStr != null) {
            vessel = vesselService.getVessel(Long.valueOf(mmsiStr));
        }

        if (vessel != null) {
            route = vesselService.getActiveRoute(Long.valueOf(mmsiStr));
            details = vessel.toJsonModel2();
            // merge AIS data
            details.getAis().putAll(result);
        } else {
            details = new VesselDetails();
            details.setAis(result);
        }

        details.setAdditionalInformation(new AdditionalInformation(route != null ? route.getEnavId() : null,
                track != null));
        return details;
    }

    @GET
    @Path("/details")
    @Produces("application/json")
    public Map details(@QueryParam("id") long vesselId, @QueryParam("past_track") int pastTrack) {
        Map result = aisViewService.vesselTargetDetails(vesselId, pastTrack);
        Route route = vesselService.getActiveRoute((long) (Integer) result.get("mmsi"));
        if (route != null) {
            result.put("route", route.toJsonModel());
        }
        return result;
    }
}
