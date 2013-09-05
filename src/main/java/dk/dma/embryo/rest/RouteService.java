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
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.slf4j.Logger;

import dk.dma.arcticweb.service.ShipService;
import dk.dma.embryo.security.authorization.YourShip;
import dk.dma.enav.model.voyage.Route;

/**
 * 
 * @author Jesper Tejlgaard
 */
@Path("/route")
public class RouteService {

    @Inject
    private ShipService shipService;

    @Inject
    private Logger logger;

    public RouteService() {
    }

    @GET
    @Path("/{id}")
    @Produces("application/json")
    public Route getRoute(@PathParam("id") String id) {
        logger.debug("getRoute({})", id);
        dk.dma.embryo.domain.Route route = shipService.getRouteByEnavId(id);

        // TODO replace below with some http status telling resource is not available
        return route != null ? route.toEnavModel() : null;
    }

    /**
     * Given that mmsi value is supplied, the active route for that ship is returned
     * 
     * Given that no mmsi value is supplied, the active route for the authorized user (with {@link YourShip} permission)
     * is returned.
     * 
     * @param mmsi
     * @return
     */

    @GET
    @Path("/active{mmsi : (/mmsi)?}")
    @Produces("application/json")
    public Route getActive(@PathParam("mmsi") String mmsi) {
        logger.debug("getActive({})", mmsi);

        dk.dma.embryo.domain.Route route;

        if (mmsi == null || mmsi.trim().length() == 0) {
            route = shipService.getYourActiveRoute();
        } else {
            route = shipService.getActiveRoute(Long.valueOf(mmsi));
        }

        Route result = route != null ? route.toEnavModel() : null;
        
        logger.debug("getActive({}) : {}", mmsi, result);
        // TODO replace below with some http status telling resource is not available
        return result;
    }

    @PUT
    @Consumes("application/json")
    public void save(Route route) {
        logger.debug("save({})", route);

        dk.dma.embryo.domain.Route toBeSaved = dk.dma.embryo.domain.Route.fromEnavModel(route);
        shipService.saveRoute(toBeSaved);
        // String result = "Product created : " + product;
        // return Response.status(201).entity(result).build();
    }

    // TODO Remove, when wicket has been removed
    @POST
    @Consumes("application/json")
    public void save2(Route route) {
        logger.debug("save2({})", route);

        dk.dma.embryo.domain.Route toBeSaved = dk.dma.embryo.domain.Route.fromEnavModel(route);
        shipService.saveRoute(toBeSaved);
        // String result = "Product created : " + product;
        // return Response.status(201).entity(result).build();
    }

    /*
     * FIXME This method does not follow rest principles. What should have been done, was to save a ship with current
     * voyage, which had an activeRoute. 
     */
    @PUT
    @Path("/activate")
    @Produces("application/json")
    public void activate(String routeId) {
        logger.debug("Activating route: {}", routeId);

        shipService.activateRoute(routeId);
        // String result = "Product created : " + product;
        // return Response.status(201).entity(result).build();
    }

}
