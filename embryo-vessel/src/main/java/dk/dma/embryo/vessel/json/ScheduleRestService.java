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

import java.util.List;

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

import dk.dma.embryo.vessel.json.Voyage.RouteOverview;
import dk.dma.embryo.vessel.model.Route;
import dk.dma.embryo.vessel.model.Voyage;
import dk.dma.embryo.vessel.service.ScheduleService;

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
    @NoCache
    public dk.dma.embryo.vessel.json.ScheduleResponse getScheduleView(@PathParam("mmsi") Long mmsi) {
        logger.trace("getScheduleView({})", mmsi);

        List<Voyage> schedule = scheduleService.getSchedule(mmsi);
        dk.dma.embryo.vessel.json.ScheduleResponse result = new dk.dma.embryo.vessel.json.ScheduleResponse();
        if (schedule != null) {
            dk.dma.embryo.vessel.json.Voyage[] voyages = new dk.dma.embryo.vessel.json.Voyage[schedule.size()];
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
