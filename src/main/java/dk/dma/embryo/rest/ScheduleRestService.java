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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.slf4j.Logger;

import dk.dma.arcticweb.service.ScheduleService;
import dk.dma.embryo.domain.Schedule;

@Path("/schedule")
public class ScheduleRestService {

    @Inject
    private ScheduleService scheduleService;

    @Inject
    private Logger logger;

    public ScheduleRestService() {
    }

    @GET
    @Path("/overview/{mmsi}")
    @Produces("application/json")
    public dk.dma.embryo.rest.json.Schedule getScheduleView(@PathParam("mmsi") Long mmsi) {
        logger.trace("getScheduleView({})", mmsi);

        Schedule schedule = scheduleService.getSchedule(mmsi);
        dk.dma.embryo.rest.json.Schedule result = null;
        if (schedule != null) {
            result = schedule.toJsonModel();
        }

        logger.debug("getScheduleView({}) : {}", mmsi, result);
        return result;
    }
    
    @PUT
    @Path("/save")
    @Consumes("application/json")
    public void savePlan(dk.dma.embryo.rest.json.Schedule schedule) {
        logger.trace("savePlan({})", schedule);

        Schedule toBeSaved = Schedule.fromJsonModel(schedule);
        scheduleService.saveSchedule(toBeSaved);

        logger.trace("savePlan()");
    }

}
