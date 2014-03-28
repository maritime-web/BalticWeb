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
package dk.dma.embryo.vessel.json;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import dk.dma.embryo.vessel.service.ScheduleService;

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
    @NoCache
    public Route getRoute(@PathParam("id") String id) {
        logger.debug("getRoute({})", id);
        dk.dma.embryo.vessel.model.Route route = scheduleService.getRouteByEnavId(id);

        return route != null ? route.toJsonModel() : null;
    }

    @GET
    @Path("/active/{mmsi}")
    @Produces("application/json")
    @GZIP
    @NoCache
    public Route getActive(@PathParam("mmsi") String mmsi) {
        logger.debug("getActive({})", mmsi);

        dk.dma.embryo.vessel.model.Route route;

        route = scheduleService.getActiveRoute(Long.valueOf(mmsi));

        Route result = route != null ? route.toJsonModel() : null;

        logger.debug("getActive({}) : {}", mmsi, result);
        return result;
    }

    @PUT
    @Consumes("application/json")
    public void save(SaveRouteRequest request) {
        logger.debug("save({})", request);

        dk.dma.embryo.vessel.model.Route toBeSaved = dk.dma.embryo.vessel.model.Route.fromJsonModel(request.getRoute());
        scheduleService.saveRoute(toBeSaved, request.getVoyageId(), false);
    }

    @PUT
    @Path("/save/activate")
    @Consumes("application/json")
    public void saveAndActivate(SaveRouteRequest request) {
        logger.debug("saveAndActivate({})", request);

        dk.dma.embryo.vessel.model.Route toBeSaved = dk.dma.embryo.vessel.model.Route.fromJsonModel(request.getRoute());
        scheduleService.saveRoute(toBeSaved, request.getVoyageId(), true);
    }

    @PUT
    @Path("/activate")
    @Consumes("application/json")
    public void activate(ActiveRoute activeRoute) {
        logger.debug("Activating route: {}", activeRoute);

        scheduleService.activateRoute(activeRoute.getRouteId(), activeRoute.getActive());
    }

}
