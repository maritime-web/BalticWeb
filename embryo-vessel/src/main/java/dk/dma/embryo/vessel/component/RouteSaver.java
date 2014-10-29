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

import java.util.List;

import dk.dma.embryo.vessel.model.Route;
import dk.dma.embryo.vessel.model.Vessel;
import dk.dma.embryo.vessel.model.Voyage;
import dk.dma.embryo.vessel.persistence.ScheduleDao;

/**
 * @author Jesper Tejlgaard
 */
public class RouteSaver {

    
    private ScheduleDao scheduleRepository;
    
    public RouteSaver(ScheduleDao repository){
        this.scheduleRepository = repository;
    }
    
    /**
     * Used by other service classes, e.g. TestServiceBean. Does not require login.
     * 
     * @param route
     * @param voyageId
     * @param active
     * @return
     */
    public String saveRoute(Route route, String voyageId, Boolean active) {
        if (route.getId() == null) {
            Long id = scheduleRepository.getRouteId(route.getEnavId());
            route.setId(id);
        }

        if (voyageId == null) {
            throw new IllegalArgumentException("Missing 'voyageId'");
        }

        Voyage voyage = scheduleRepository.getVoyageByEnavId(voyageId);
        if (voyage == null) {
            throw new IllegalArgumentException("Unknown 'voyageId' value '" + voyageId + "'");
        }

        if(route.getOrigin() == null){
            route.setOrigin(voyage.getLocation());
        }

        List<Voyage> voyages = scheduleRepository.getSchedule(voyage.getVessel().getMmsi());
        
        int count = 0;
        boolean found = false;
        while (count < voyages.size() && !found) {
            Voyage v = voyages.get(count++);
            if (v.getEnavId().equals(voyage.getEnavId())) {
                found = true;
            }
        }
        if (count < voyages.size()) {
            route.setDestination(voyages.get(count).getLocation());
        }

        route.setVoyage(voyage);
        scheduleRepository.saveEntity(route);
        // update relation
        scheduleRepository.saveEntity(voyage);

        if (active == Boolean.TRUE) {
            Vessel vessel = voyage.getVessel();
            vessel.setActiveVoyage(voyage);
            scheduleRepository.saveEntity(vessel);
        }

        return route.getEnavId();
    }

}
