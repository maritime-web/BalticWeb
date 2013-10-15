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

import dk.dma.arcticweb.service.ShipService;
import dk.dma.embryo.domain.Route;
import dk.dma.embryo.restclients.AisViewService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.Map;

@Path("/vessel")
public class VesselRestService {
    @Inject
    AisViewService aisViewService;

    @Inject
    ShipService shipService;

    @GET
    @Path("/details")
    @Produces("application/json")
    public Map details(@QueryParam("id") long vesselId, @QueryParam("past_track") int pastTrack) {
        Map result =  aisViewService.vesselTargetDetails(vesselId, pastTrack);
        Route route = shipService.getActiveRoute((long) (Integer) result.get("mmsi"));
        if (route != null) {
            result.put("route", route.toJsonModel());
        }
        return result;
    }
}
