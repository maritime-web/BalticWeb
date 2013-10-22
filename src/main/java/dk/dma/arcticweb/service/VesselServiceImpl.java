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
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;

import dk.dma.arcticweb.dao.RealmDao;
import dk.dma.arcticweb.dao.VesselDao;
import dk.dma.embryo.domain.Route;
import dk.dma.embryo.domain.Sailor;
import dk.dma.embryo.domain.Vessel;
import dk.dma.embryo.domain.Voyage;
import dk.dma.embryo.domain.VoyagePlan;
import dk.dma.embryo.security.Subject;
import dk.dma.embryo.security.authorization.YourShip;
import dk.dma.enav.serialization.RouteParser;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class VesselServiceImpl implements VesselService {

    @Inject
    private VesselDao vesselRepository;

    @Inject
    private RealmDao realmDao;

    @Inject
    private Subject subject;

    @Inject
    private Logger logger;

    public VesselServiceImpl() {
    }

    public VesselServiceImpl(VesselDao vesselRepository) {
        this.vesselRepository = vesselRepository;
    }

    @Override
    @YourShip
    public String save(Vessel vessel) {
        Vessel managed = vesselRepository.getVesselByMaritimeId(vessel.getMaritimeId());

        if (managed != null) {
            // copying all values to managed entity to avoid resetting JPA association fields.
            managed.getAisData().setName(vessel.getAisData().getName());
            managed.setMmsi(vessel.getMmsi());
            managed.getAisData().setImoNo(vessel.getAisData().getImoNo());
            managed.getAisData().setCallsign(vessel.getAisData().getCallsign());
            managed.setCommCapabilities(vessel.getCommCapabilities());
            managed.setHelipad(vessel.getHelipad());
            managed.getAisData().setLength(vessel.getAisData().getLength());
            managed.setMaxSpeed(vessel.getMaxSpeed());
            managed.getAisData().setWidth(vessel.getAisData().getWidth());
            managed.setType(vessel.getType());
            managed.setGrossTonnage(vessel.getGrossTonnage());

            managed = vesselRepository.saveEntity(managed);
            return managed.getMaritimeId();
        } else {
            vessel = vesselRepository.saveEntity(vessel);
            return vessel.getMaritimeId();
        }
    }

    @Override
    public void saveVoyagePlan(VoyagePlan toBeUpdated) {
        VoyagePlan fresh = null;

        if (toBeUpdated.getId() != null) {
            fresh = vesselRepository.getByPrimaryKey(VoyagePlan.class, toBeUpdated.getId());
        }

        if (fresh == null) {
            // Voyage Plan does not exist in the database. Create a new Voyage Plan
            fresh = new VoyagePlan();
        }

        Map<String, Voyage> voyagePlanToBeUpdated = toBeUpdated.getVoyagePlanAsMap();
        Map<String, Voyage> freshVoyagePlan = fresh.getVoyagePlanAsMap();

        Set<String> toBeDeleted = new HashSet<>(freshVoyagePlan.keySet());
        toBeDeleted.removeAll(voyagePlanToBeUpdated.keySet());

        List<Voyage> newVoyages = new LinkedList<>();

        for (Entry<String, Voyage> voyageEntry : voyagePlanToBeUpdated.entrySet()) {
            Voyage v = null;
            if (freshVoyagePlan.containsKey(voyageEntry.getKey())) {
                v = freshVoyagePlan.get(voyageEntry.getKey());
            } else {
                v = new Voyage();
                fresh.addVoyageEntry(v);
                newVoyages.add(voyageEntry.getValue());
            }
            v.setArrival(voyageEntry.getValue().getArrival());
            v.setBerthName(voyageEntry.getValue().getBerthName());
            v.setDeparture(voyageEntry.getValue().getDeparture());
            v.setPosition(voyageEntry.getValue().getPosition());
            v.setCrewOnBoard(voyageEntry.getValue().getCrewOnBoard());
            v.setPassengersOnBoard(voyageEntry.getValue().getPassengersOnBoard());
            v.setDoctorOnBoard(voyageEntry.getValue().getDoctorOnBoard());
        }

        for (String key : toBeDeleted) {
            Voyage v = freshVoyagePlan.get(key);
            fresh.removeVoyage(v);
            vesselRepository.remove(v);
        }

        vesselRepository.saveEntity(fresh);
    }

    public Voyage getActiveVoyage(String maritimeVesselId) {
        Vessel vessel = vesselRepository.getVesselByMaritimeId(maritimeVesselId);
        if (vessel == null) {
            return null;
        }
        Voyage voyage = vessel.getActiveVoyage();
        return voyage;
    }

    public Vessel getYourVessel() {
        if (subject.hasRole(Sailor.class)) {
            Sailor sailor = realmDao.getSailor(subject.getUserId());
            return sailor.getVessel();
        }
        return new Vessel();
    }

    @YourShip
    @Override
    public VoyagePlan getVoyagePlan(Long mmsi) {
        VoyagePlan voyagePlan = vesselRepository.getVoyagePlan(mmsi);
        if (voyagePlan == null) {
            voyagePlan = new VoyagePlan();
            // FIXME: Hack only works for YourShip feature
            Vessel vessel = vesselRepository.getVessel(subject.getRole(Sailor.class));
            vessel.setVoyagePlan(voyagePlan);
        }

        for (Voyage voyage : voyagePlan.getVoyagePlan()) {
            voyage.getRoute();
        }

        return voyagePlan;
    }

    @YourShip
    @Override
    public List<Voyage> getVoyages(Long mmsi) {
        // TODO fix to work for several voyage plans
        VoyagePlan plan = vesselRepository.getVoyagePlan(mmsi);
        if (plan == null) {
            return Collections.emptyList();
        }
        return plan.getVoyagePlan();
    }

    @Override
    public Vessel getVessel(Long mmsi) {
        return vesselRepository.getVessel(mmsi);
    }

    
    @Override
    public Vessel getVessel(String maritimeId) {
        return vesselRepository.getVesselByMaritimeId(maritimeId);
    }

    /**
     * Used to save uploaded routes.
     * 
     * Automatically fills out route fields also being part of voyage information, like departure location, destination location, times etc. 
     */
    @Override
    public String saveRoute(Route route, String voyageId, Boolean active) {
        if (route.getId() == null) {
            Long id = vesselRepository.getRouteId(route.getEnavId());
            route.setId(id);
        }

        if (voyageId == null) {
            throw new IllegalArgumentException("Missing 'voyageId'");
        }

        Voyage voyage = vesselRepository.getVoyageByEnavId(voyageId);
        if (voyage == null) {
            throw new IllegalArgumentException("Unknown 'voyageId' value '" + voyageId + "'");
        }
        
        route.setOrigin(voyage.getBerthName());
        route.setEtaOfDeparture(voyage.getDeparture());
        
        List<Voyage> voyages = voyage.getPlan().getVoyagePlan();
        int count = 0;
        boolean found = false;
        while(count < voyages.size() && !found){
            Voyage v = voyages.get(count++);
            if(v.getEnavId().equals(voyage.getEnavId())){
                found = true;
            }
        }
        if(count < voyages.size()){
            route.setDestination(voyages.get(count).getBerthName());
        }

        route.setVoyage(voyage);
        vesselRepository.saveEntity(route);
        // update relation
        vesselRepository.saveEntity(voyage);

        if (active == Boolean.TRUE) {
            Vessel vessel = voyage.getPlan().getVessel();
            vessel.setActiveVoyage(voyage);
            vesselRepository.saveEntity(vessel);
        }
        
        return route.getEnavId();
    }

    @Override
    public String saveRoute(Route route) {
        if (route.getId() == null) {
            Long id = vesselRepository.getRouteId(route.getEnavId());
            route.setId(id);
        }

        vesselRepository.saveEntity(route);

        return route.getEnavId();
    }

    @Override
    public Route getActiveRoute(Long mmsi) {
        Route r = vesselRepository.getActiveRoute(mmsi);
        if (r != null) {
            if (r.getWayPoints().size() > 0) {
                r.getWayPoints().get(0);
            }
        }
        return r;
    }

    @YourShip
    @Override
    public Route getYourActiveRoute() {
        Vessel vessel = getYourVessel();
        if (vessel == null) {
            return null;
        }
        Voyage active = vessel.getActiveVoyage();
        if (active != null && active.getRoute() != null) {
            // initialize to avoid lazyinitialization exceptions
            if (active.getRoute().getWayPoints().size() > 0) {
                active.getRoute().getWayPoints().get(0);
            }
            return active.getRoute();
        }
        return null;
    }

    @YourShip
    @Override
    public Route activateRoute(String routeEnavId, Boolean activate) {
        logger.debug("activateRoute({}, {})", routeEnavId, activate);
        Route route = vesselRepository.getRouteByEnavId(routeEnavId);

        if (route == null) {
            throw new IllegalArgumentException("Unknown route with id '" + routeEnavId);
        }

        Vessel vessel = getYourVessel();

        logger.debug("Vessel:{}", vessel.getMmsi());

        if (activate) {
            vessel.setActiveVoyage(route.getVoyage());
        } else {
            vessel.setActiveVoyage(null);
        }
        vesselRepository.saveEntity(vessel);
        return route;
    }

    @Override
    public Route getRouteByEnavId(String enavId) {
        Route route = vesselRepository.getRouteByEnavId(enavId);
        return route;
    }

    @Override
    public Voyage getVoyage(String enavId) {
        return vesselRepository.getVoyageByEnavId(enavId);
    }

    /**
     * Also sets yourship on route
     */
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Route parseRoute(InputStream is) throws IOException {
        RouteParser parser = RouteParser.getSimpleRouteParser(is);

        dk.dma.enav.model.voyage.Route enavRoute = parser.parse();
        Route route = Route.fromEnavModel(enavRoute);

        return route;
    }
}
