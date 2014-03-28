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
package dk.dma.embryo.vessel.component;

import dk.dma.embryo.vessel.model.Route;
import dk.dma.embryo.vessel.model.Vessel;
import dk.dma.embryo.vessel.persistence.ScheduleDao;

/**
 * @author Jesper Tejlgaard
 */
public class RouteActivator {

    private ScheduleDao scheduleRepository;
    
    public RouteActivator(ScheduleDao repository){
        this.scheduleRepository = repository;
    }
    
    /**
     * Used by other service classes, e.g. TestServiceBean. Does not require login.
     * 
     * @param routeEnavId
     * @param activate
     * @return
     */
    public Route activateRoute(String routeEnavId, Boolean activate) {
        Route route = scheduleRepository.getRouteByEnavId(routeEnavId);

        if (route == null) {
            throw new IllegalArgumentException("Unknown route with id '" + routeEnavId);
        }

        Vessel vessel = route.getVoyage().getVessel();

        if (activate) {
            vessel.setActiveVoyage(route.getVoyage());
        } else {
            vessel.setActiveVoyage(null);
        }
        scheduleRepository.saveEntity(vessel);
        return route;
    }

}
