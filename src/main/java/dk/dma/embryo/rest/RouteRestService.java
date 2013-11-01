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

import org.jboss.resteasy.annotations.GZIP;
import org.slf4j.Logger;

import dk.dma.arcticweb.service.ScheduleService;
import dk.dma.embryo.rest.json.ActiveRoute;
import dk.dma.embryo.rest.json.Route;
import dk.dma.embryo.security.authorization.YourShip;

/**
 * 
 * @author Jesper Tejlgaard
 */
@Path("/route")
public class RouteRestService {

    @Inject
    private ScheduleService scheduleService;
    
    @Inject
    private Logger logger;

    public RouteRestService() {
    }

    @GET
    @Path("/{id}")
    @Produces("application/json")
    @GZIP
    public Route getRoute(@PathParam("id") String id) {
        logger.debug("getRoute({})", id);
        dk.dma.embryo.domain.Route route = scheduleService.getRouteByEnavId(id);

        // TODO replace below with some http status telling resource is not available
        return route != null ? route.toJsonModel() : null;
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
    @Path("/active")
    @Produces("application/json")
    @GZIP
    public Route getActive() {
        logger.debug("getActive()");

        dk.dma.embryo.domain.Route route;

        route = scheduleService.getYourActiveRoute();

        Route result = route != null ? route.toJsonModel() : null;

        logger.debug("getActive({}) : {}", result);
        // TODO replace below with some http status telling resource is not available
        return result;
    }

    @GET
    @Path("/active/{mmsi}")
    @Produces("application/json")
    @GZIP
    public Route getActiveByMmsi(@PathParam("mmsi") String mmsi) {
        logger.debug("getActiveByMmsi({})", mmsi);

        dk.dma.embryo.domain.Route route;

        route = scheduleService.getActiveRoute(Long.valueOf(mmsi));

        Route result = route != null ? route.toJsonModel() : null;

        logger.debug("getActiveByMmsi({}) : {}", mmsi, result);
        // TODO replace below with some http status telling resource is not available
        return result;
    }

    @PUT
    @Consumes("application/json")
    public void save(Route route) {
        logger.debug("save({})", route);

        dk.dma.embryo.domain.Route toBeSaved = dk.dma.embryo.domain.Route.fromJsonModel(route);
        scheduleService.saveRoute(toBeSaved);
        // String result = "Product created : " + product;
        // return Response.status(201).entity(result).build();
    }

    // TODO Remove, when wicket has been removed
    @POST
    @Consumes("application/json")
    public void save2(Route route) {
        logger.debug("save2({})", route);

        dk.dma.embryo.domain.Route toBeSaved = dk.dma.embryo.domain.Route.fromJsonModel(route);
        scheduleService.saveRoute(toBeSaved);
        // String result = "Product created : " + product;
        // return Response.status(201).entity(result).build();
    }

    /*
     * FIXME This method does not follow rest principles. What should have been done, was to save a ship with current
     * voyage, which had an activeRoute.
     */
    @PUT
    @Path("/activate")
    @Consumes("application/json")
    public void activate(ActiveRoute activeRoute) {
        logger.debug("Activating route: {}", activeRoute);

        scheduleService.activateRoute(activeRoute.getRouteId(), activeRoute.getActive());
        // String result = "Product created : " + product;
        // return Response.status(201).entity(result).build();
    }

}
