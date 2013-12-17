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
package dk.dma.embryo.component;

import java.util.List;

import dk.dma.arcticweb.dao.ScheduleDao;
import dk.dma.embryo.domain.Route;
import dk.dma.embryo.domain.Vessel;
import dk.dma.embryo.domain.Voyage;

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
            route.setOrigin(voyage.getBerthName());
        }
        if(route.getEtaOfDeparture() == null){
            route.setEtaOfDeparture(voyage.getDeparture());
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
            route.setDestination(voyages.get(count).getBerthName());
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
