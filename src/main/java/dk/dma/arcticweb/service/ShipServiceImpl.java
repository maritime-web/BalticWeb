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

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;

import dk.dma.arcticweb.dao.RealmDao;
import dk.dma.arcticweb.dao.ShipDao;
import dk.dma.embryo.domain.Route;
import dk.dma.embryo.domain.Sailor;
import dk.dma.embryo.domain.Ship;
import dk.dma.embryo.domain.ShipReport;
import dk.dma.embryo.domain.Voyage;
import dk.dma.embryo.domain.VoyagePlan;
import dk.dma.embryo.security.Subject;
import dk.dma.embryo.security.authorization.YourShip;
import dk.dma.enav.serialization.RouteParser;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ShipServiceImpl implements ShipService {

    @Inject
    private ShipDao shipRepository;

    @Inject
    private RealmDao realmDao;

    @Inject
    private Subject subject;

    @Inject
    private Logger logger;

    public ShipServiceImpl() {
    }

    public ShipServiceImpl(ShipDao shipRepository) {
        this.shipRepository = shipRepository;
    }

    // TODO implement Security Interceptor for EJB methods
    @Override
    @YourShip
    public void reportForCurrentShip(ShipReport shipReport) {
        Ship ship = subject.getRole(Sailor.class).getShip();

        // TODO Should report time be modified
        shipReport.setReportTime(new Date());
        shipReport.setCreated(new Date());
        shipReport.setShip(ship);

        shipRepository.saveEntity(shipReport);
    }

    @Override
    @YourShip
    public String save(Ship ship) {
        Ship managed = shipRepository.getShipByMaritimeId(ship.getMaritimeId());

        if (managed != null) {
            // copying all values to managed entity to avoid resetting JPA association fields.
            managed.setName(ship.getName());
            managed.setMmsi(ship.getMmsi());
            managed.setImoNo(ship.getImoNo());
            managed.setCallsign(ship.getCallsign());
            managed.setCommCapabilities(ship.getCommCapabilities());
            managed.setHelipad(ship.getHelipad());
            managed.setLength(ship.getLength());
            managed.setMaxSpeed(ship.getMaxSpeed());
            managed.setWidth(ship.getWidth());
            managed.setRescueCapacity(ship.getRescueCapacity());
            managed.setType(ship.getType());
            managed.setTonnage(ship.getTonnage());

            managed = shipRepository.saveEntity(managed);
            return managed.getMaritimeId();
        } else {
            ship = shipRepository.saveEntity(ship);
            return ship.getMaritimeId();
        }
    }

    @Override
    public void saveVoyagePlan(VoyagePlan toBeUpdated) {
        VoyagePlan fresh = null;

        if (toBeUpdated.getId() != null) {
            fresh = shipRepository.getByPrimaryKey(VoyagePlan.class, toBeUpdated.getId());
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
            v.setPersonsOnBoard(voyageEntry.getValue().getPersonsOnBoard());
            v.setDoctorOnBoard(voyageEntry.getValue().getDoctorOnBoard());
        }

        for (String key : toBeDeleted) {
            Voyage v = freshVoyagePlan.get(key);
            fresh.removeVoyage(v);
            shipRepository.remove(v);
        }

        shipRepository.saveEntity(fresh);
    }

    public Voyage getActiveVoyage(String maritimeShipId) {
        Ship ship = shipRepository.getShipByMaritimeId(maritimeShipId);
        if (ship == null) {
            return null;
        }
        Voyage voyage = ship.getActiveVoyage();
        return voyage;
    }

    public Ship getYourShip() {
        if (subject.hasRole(Sailor.class)) {
            Sailor sailor = realmDao.getSailor(subject.getUserId());
            return sailor.getShip();
        }
        return new Ship();
    }

    @YourShip
    @Override
    public VoyagePlan getVoyagePlan(Long mmsi) {
        VoyagePlan voyagePlan = shipRepository.getVoyagePlan(mmsi);
        if (voyagePlan == null) {
            voyagePlan = new VoyagePlan();
            // FIXME: Hack only works for YourShip feature
            Ship ship = shipRepository.getShip(subject.getRole(Sailor.class));
            ship.setVoyagePlan(voyagePlan);
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
        VoyagePlan plan = shipRepository.getVoyagePlan(mmsi);
        if (plan == null) {
            return Collections.emptyList();
        }
        return plan.getVoyagePlan();
    }

    @Override
    public String saveRoute(Route route, String voyageId, boolean active) {
        if (route.getId() == null) {
            Long id = shipRepository.getRouteId(route.getEnavId());
            route.setId(id);
        }

        if (voyageId != null) {
            Voyage voyage = shipRepository.getVoyageByEnavId(voyageId);
            route.setVoyage(voyage);
        }

        shipRepository.saveEntity(route);

        return route.getEnavId();
    }

    @Override
    public String saveRoute(Route route) {
        if (route.getId() == null) {
            Long id = shipRepository.getRouteId(route.getEnavId());
            route.setId(id);
        }

        shipRepository.saveEntity(route);

        return route.getEnavId();
    }

    @Override
    public Route getActiveRoute(Long mmsi) {
        return shipRepository.getActiveRoute(mmsi);
    }

    @YourShip
    @Override
    public Route getYourActiveRoute() {
        Ship ship = getYourShip();
        if (ship == null) {
            return null;
        }
        Voyage active = ship.getActiveVoyage();
        if (active != null && active.getRoute() != null) {
            // initialize to avoid lazyinitialization exceptions
            if (active.getRoute().getWayPoints().size() > 0) {
                active.getRoute().getWayPoints().get(0);
            }
            return active.getRoute();
        }
        return null;
    }

    @Override
    public Route activateRoute(String routeEnavId) {
        logger.debug("activateRoute({})", routeEnavId);
        Route route = shipRepository.getRouteByEnavId(routeEnavId);
        Voyage v = route.getVoyage();
        Ship ship = route.getShip();
        ship.setActiveVoyage(v);
        shipRepository.saveEntity(ship);
        return route;
    }

    @Override
    public Route getRouteByEnavId(String enavId) {
        Route route = shipRepository.getRouteByEnavId(enavId);
        return route;
    }

    @Override
    public Voyage getVoyage(String enavId) {
        return shipRepository.getVoyageByEnavId(enavId);
    }

    /**
     * Also sets yourship on route
     */
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Route parseRoute(InputStream is) throws IOException {
        RouteParser parser = RouteParser.getSimpleRouteParser(is);

        dk.dma.enav.model.voyage.Route enavRoute = parser.parse();
        Route route = Route.fromEnavModel(enavRoute);
        route.setShip(getYourShip());

        return route;
    }
}
