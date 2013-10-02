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

import java.util.Date;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;

import dk.dma.arcticweb.dao.ShipDao;
import dk.dma.embryo.domain.Route;
import dk.dma.embryo.domain.Ship;
import dk.dma.embryo.domain.WayPoint;
import dk.dma.embryo.restclients.DmiSejlRuteService;
import dk.dma.embryo.restclients.DmiSejlRuteService.Waypoint;
import dk.dma.embryo.security.Subject;

@Stateless
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class MetocServiceImpl implements MetocService {

    @Inject
    private DmiSejlRuteService dmiSejlRuteService;
    
    @Inject
    private Subject subject;

    @Inject
    private ShipDao shipDao;
    
    @Inject Logger logger;

    public MetocServiceImpl() {
    }

    public MetocServiceImpl(ShipDao shipDao, Subject subject) {
        this.shipDao = shipDao;
        this.subject = subject;
    }

    @Override
    public DmiSejlRuteService.SejlRuteResponse getMetoc(String routeId) {
        Route route = shipDao.getRouteByEnavId(routeId);
        if (route == null) {
            throw new IllegalArgumentException("Unknown route id: " + routeId);
        }

        Ship ship = route.getVoyage().getPlan().getShip();

        DmiSejlRuteService.SejlRuteRequest request = new DmiSejlRuteService.SejlRuteRequest();
        request.setMssi(ship.getMmsi());
        request.setDatatypes(new String[] { "sealevel", "current", "wave", "wind", "density" });
        request.setDt(60);

        Waypoint[] waypoints = new Waypoint[route.getWayPoints().size()];
        int count = 0;

        for (WayPoint waypoint : route.getWayPoints()) {
            waypoints[count] = new DmiSejlRuteService.Waypoint();
            waypoints[count].setEta(DmiSejlRuteService.DATE_FORMAT.format(new Date(
                    System.currentTimeMillis() + 1000L * 3600 * count)));
            waypoints[count].setHeading("RL");
            waypoints[count].setLat(waypoint.getPosition().getLatitude());
            waypoints[count].setLon(waypoint.getPosition().getLongitude());
            count++;
        }

        request.setWaypoints(waypoints);

        logger.debug("Sending METOC request: {}", request);
        
        DmiSejlRuteService.SejlRuteResponse sejlRuteResponse = dmiSejlRuteService.sejlRute(request);

        logger.debug("Received METOC response: {}", sejlRuteResponse);

        return sejlRuteResponse;
    }

}
