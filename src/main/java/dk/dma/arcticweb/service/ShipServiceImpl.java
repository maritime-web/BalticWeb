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
import dk.dma.embryo.domain.Ship2;
import dk.dma.embryo.domain.ShipReport;
import dk.dma.embryo.domain.Voyage;
import dk.dma.embryo.domain.VoyageInformation2;
import dk.dma.embryo.domain.transformers.RouteTransformer;
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
    
    @Inject Logger logger;

    public ShipServiceImpl() {
    }

    public ShipServiceImpl(ShipDao shipRepository) {
        this.shipRepository = shipRepository;
    }

    // TODO implement Security Interceptor for EJB methods
    @Override
    @YourShip
    public void reportForCurrentShip(ShipReport shipReport) {
        Ship2 ship = subject.getRole(Sailor.class).getShip();

        // TODO Should report time be modified
        shipReport.setReportTime(new Date());
        shipReport.setCreated(new Date());
        shipReport.setShip(ship);

        shipRepository.saveEntity(shipReport);
    }

    @Override
    public void saveVoyageInformation(VoyageInformation2 toBeUpdated) {

        VoyageInformation2 fresh = getVoyageInformation(toBeUpdated.getShip().getMmsi());
        fresh.setDoctorOnboard(toBeUpdated.getDoctorOnboard());
        fresh.setPersonsOnboard(toBeUpdated.getPersonsOnboard());

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

    public Ship2 getYourShip() {
        if (subject.hasRole(Sailor.class)) {
            Sailor sailor = realmDao.getSailor(subject.getUserId());
            return sailor.getShip();
        }
        return new Ship2();
    }

    @YourShip
    @Override
    public VoyageInformation2 getVoyageInformation(Long mmsi) {
        VoyageInformation2 voyageInfo = shipRepository.getVoyageInformation(mmsi);
        if (voyageInfo == null) {
            voyageInfo = new VoyageInformation2();
            // FIXME: Hack only works for YourShip feature
            Ship2 ship = shipRepository.getShip(subject.getRole(Sailor.class));
            ship.setVoyageInformation(voyageInfo);
        }
        return voyageInfo;
    }

    @Override
    public void saveRoute(Route route) {
        logger.debug("SAVING ROUTE {}", route);
        
        shipRepository.saveEntity(route);
    }

    @Override
    public Route getActiveRoute(Long mmsi) {
        return shipRepository.getActiveRoute(mmsi);
    }


    @Override
    public Voyage getVoyage(Long id) {
        return shipRepository.getByPrimaryKey(Voyage.class, id);
    }
    
    public Route parseRoute(InputStream is) throws IOException{
        RouteParser parser = RouteParser.getSimpleRouteParser(is);
        
        dk.dma.enav.model.voyage.Route enavRoute = parser.parse();
        
        Route route = new RouteTransformer().transform(enavRoute);
        
        route.setShip(getYourShip());
        
        return route;
    }
}
