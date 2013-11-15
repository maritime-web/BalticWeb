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
package dk.dma.arcticweb.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;

import dk.dma.arcticweb.dao.RealmDao;
import dk.dma.arcticweb.dao.ScheduleDao;
import dk.dma.arcticweb.dao.VesselDao;
import dk.dma.embryo.domain.Route;
import dk.dma.embryo.domain.Vessel;
import dk.dma.embryo.domain.Voyage;
import dk.dma.embryo.domain.WayPoint;
import dk.dma.embryo.security.Subject;
import dk.dma.embryo.security.authorization.YourShip;
import dk.dma.enav.serialization.RouteParser;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ScheduleServiceImpl implements ScheduleService {

    @Inject
    private ScheduleDao scheduleRepository;

    @Inject
    private VesselService vesselService;

    @Inject
    private VesselDao vesselRepository;

    @Inject
    private RealmDao realmDao;

    @Inject
    private Subject subject;

    @Inject
    private Logger logger;

    public ScheduleServiceImpl() {
    }

    public ScheduleServiceImpl(ScheduleDao scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    @Override
    public void updateSchedule(Long mmsi, List<Voyage> toBeSaved, String[] toBeDeleted) {

        List<String> ids = new ArrayList<>(toBeSaved.size());
        for (int i = 0; i < toBeSaved.size(); i++) {
            ids.add(toBeSaved.get(i).getEnavId());
        }

        List<Voyage> persisted = scheduleRepository.getByEnavIds(ids);
        Map<String, Voyage> persistedAsMap = Voyage.asMap(persisted);

        Vessel vessel = vesselRepository.getVessel(mmsi);

        // In order to maintain JPA relations we have to select from DB and merge data manually
        for (Voyage voyage : toBeSaved) {
            Voyage v = null;
            if (persistedAsMap.containsKey(voyage.getEnavId())) {
                v = persistedAsMap.get(voyage.getEnavId());

                v.setArrival(voyage.getArrival());
                v.setBerthName(voyage.getBerthName());
                v.setDeparture(voyage.getDeparture());
                v.setPosition(voyage.getPosition());
                v.setCrewOnBoard(voyage.getCrewOnBoard());
                v.setPassengersOnBoard(voyage.getPassengersOnBoard());
                v.setDoctorOnBoard(voyage.getDoctorOnBoard());
            } else {
                v = voyage;
                vessel.addVoyageEntry(v);
            }
            scheduleRepository.saveEntity(v);
        }

        if (toBeDeleted.length > 0) {
            List<String> toBeDeletedAsList = Arrays.asList(toBeDeleted);
            List<Voyage> toDelete = scheduleRepository.getByEnavIds(toBeDeletedAsList);
            for (Voyage voyage : toDelete) {
                scheduleRepository.remove(voyage);
            }
        }
    }

    @YourShip
    @Override
    public List<Voyage> getSchedule(Long mmsi) {
        List<Voyage> schedule = scheduleRepository.getSchedule(mmsi);

        for (Voyage voyage : schedule) {
            Route r = voyage.getRoute();
            if (r != null) {
                for (WayPoint w : r.getWayPoints()) {
                    w.getName();
                }

            }
        }

        return schedule;
    }

    /**
     * Used to save uploaded routes.
     * 
     * Automatically fills out route fields also being part of voyage information, like departure location, destination
     * location, times etc.
     */
    @Override
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

        List<Voyage> voyages = voyage.getVessel().getSchedule();
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

    @Override
    public String saveRoute(Route route) {
        if (route.getId() == null) {
            Long id = scheduleRepository.getRouteId(route.getEnavId());
            route.setId(id);
        }

        scheduleRepository.saveEntity(route);

        return route.getEnavId();
    }

    @Override
    public Route getActiveRoute(Long mmsi) {
        Route r = scheduleRepository.getActiveRoute(mmsi);
        if (r != null) {
            if (r.getWayPoints().size() > 0) {
                r.getWayPoints().get(0);
            }
        }
        return r;
    }


    @YourShip
    @Override
    public Route activateRoute(String routeEnavId, Boolean activate) {
        logger.debug("activateRoute({}, {})", routeEnavId, activate);
        Route route = scheduleRepository.getRouteByEnavId(routeEnavId);

        if (route == null) {
            throw new IllegalArgumentException("Unknown route with id '" + routeEnavId);
        }

        Vessel vessel = vesselService.getYourVessel();

        logger.debug("Vessel:{}", vessel.getMmsi());

        if (activate) {
            vessel.setActiveVoyage(route.getVoyage());
        } else {
            vessel.setActiveVoyage(null);
        }
        scheduleRepository.saveEntity(vessel);
        return route;
    }

    @Override
    public Route getRouteByEnavId(String enavId) {
        Route route = scheduleRepository.getRouteByEnavId(enavId);
        return route;
    }

    /**
     * Also sets yourship on route
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Route parseRoute(String fileName, InputStream is, Map<String, String> context) throws IOException {
        RouteParser parser = RouteParser.getRouteParser(fileName, is, context);

        dk.dma.enav.model.voyage.Route enavRoute = parser.parse();
        Route route = Route.fromEnavModel(enavRoute);

        return route;
    }
}
