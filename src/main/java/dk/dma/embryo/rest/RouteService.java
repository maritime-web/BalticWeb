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

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.slf4j.Logger;

import dk.dma.arcticweb.service.ShipService;
import dk.dma.enav.model.voyage.Route;

@Path("/route")
public class RouteService {
    
    @Inject
    private ShipService shipService;
    
    @Inject Logger logger;
    
    public RouteService(){
        //logger.error("RouteService INsTALLED!!!!!!!!!!!!!!!!!!!!!!");
    }

    @GET
    @Path("/current/{mmsi}")
    @Produces("application/json")
    public Route getCurrentRoute(@PathParam("mmsi") Long mmsi) {
        dk.dma.embryo.domain.Route route = shipService.getActiveRoute(mmsi);
        return route.toEnavModel();
    }

    @GET
    @Path("/byId/{id}")
    @Produces("application/json")
    public Route getRoute(@PathParam("id") Long id) {
        dk.dma.embryo.domain.Route route = shipService.getRoute(id);
        return route.toEnavModel();
    }
}
