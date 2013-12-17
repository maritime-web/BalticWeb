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

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.jboss.resteasy.annotations.GZIP;
import org.slf4j.Logger;

import dk.dma.embryo.domain.Route;
import dk.dma.embryo.domain.Voyage;
import dk.dma.embryo.rest.json.ScheduleRequest;
import dk.dma.embryo.rest.json.Voyage.RouteOverview;
import dk.dma.embryo.service.ScheduleService;

@Path("/schedule")
public class ScheduleRestService {

    @Inject
    private ScheduleService scheduleService;

    @Inject
    private Logger logger;

    public ScheduleRestService() {
    }

    @GET
    @Path("/{mmsi}")
    @Produces("application/json")
    @GZIP
    public dk.dma.embryo.rest.json.ScheduleResponse getScheduleView(@PathParam("mmsi") Long mmsi) {
        logger.trace("getScheduleView({})", mmsi);

        List<Voyage> schedule = scheduleService.getSchedule(mmsi);
        dk.dma.embryo.rest.json.ScheduleResponse result = new dk.dma.embryo.rest.json.ScheduleResponse();
        if (schedule != null) {
            dk.dma.embryo.rest.json.Voyage[] voyages = new dk.dma.embryo.rest.json.Voyage[schedule.size()];
            for (int i = 0; i < schedule.size(); i++) {
                Voyage voyage = schedule.get(i);
                voyages[i] = voyage.toJsonModel();

                if (voyage.getRoute() != null) {
                    Route route = voyage.getRoute();
                    RouteOverview overview = new RouteOverview(route.getEnavId(), route.getName(), route.getOrigin(),
                            route.getDestination(), route.getWayPoints().size());
                    voyages[i].setRoute(overview);
                }
            }

            result.setVoyages(voyages);
        }

        logger.debug("getScheduleView({}) : {}", mmsi, result);
        return result;
    }

    @PUT
    @Path("/save")
    @Consumes("application/json")
    public void save(ScheduleRequest scheduleRequest) {
        logger.debug("savePlan({})", scheduleRequest);

        List<Voyage> toBeSaved = Voyage.fromJsonModel(scheduleRequest.getVoyages());
        scheduleService.updateSchedule(scheduleRequest.getMmsi(), toBeSaved, scheduleRequest.getToDelete());

        logger.debug("savePlan()");
    }

}
