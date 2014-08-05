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
