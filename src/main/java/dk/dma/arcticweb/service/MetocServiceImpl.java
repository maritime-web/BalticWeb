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

import java.util.ArrayList;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;

import dk.dma.arcticweb.dao.VesselDao;
import dk.dma.commons.model.RouteDecorator;
import dk.dma.configuration.Property;
import dk.dma.embryo.domain.Route;
import dk.dma.embryo.domain.Vessel;
import dk.dma.embryo.restclients.DmiSejlRuteService;
import dk.dma.embryo.restclients.DmiSejlRuteService.Forecast;
import dk.dma.embryo.restclients.DmiSejlRuteService.Waypoint;
import dk.dma.embryo.security.Subject;
import dk.dma.enav.model.geometry.Position;

@Stateless
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class MetocServiceImpl {

    @Inject
    private DmiSejlRuteService dmiSejlRuteService;

    @Inject
    private Subject subject;

    @Inject
    private VesselDao vesselDao;

    @Inject
    private Logger logger;

    @Inject
    @Property("embryo.metoc.minDistance")
    private Integer minimumMetocDistance;
    
    public MetocServiceImpl() {
    }

    public MetocServiceImpl(VesselDao vesselDao, Subject subject) {
        this.vesselDao = vesselDao;
        this.subject = subject;
    }

//    @Override
    public DmiSejlRuteService.SejlRuteResponse getMetoc(String routeId) {
        Route route = vesselDao.getRouteByEnavId(routeId);
        if (route == null) {
            throw new IllegalArgumentException("Unknown route id: " + routeId);
        }

        Vessel vessel = route.getVoyage().getVessel();

        RouteDecorator r = new RouteDecorator(route.toEnavModel());

        DmiSejlRuteService.SejlRuteRequest request = new DmiSejlRuteService.SejlRuteRequest();
        request.setMssi(vessel.getMmsi());
        request.setDatatypes(new String[] { "sealevel", "current", "wave", "wind", "density" });
        request.setDt(60);

        Waypoint[] waypoints = new Waypoint[r.getWaypoints().size()];
        int count = 0;

        for (RouteDecorator.Waypoint waypoint : r.getWaypoints()) {
            waypoints[count] = new DmiSejlRuteService.Waypoint();
            waypoints[count].setEta(DmiSejlRuteService.DATE_FORMAT.format(waypoint.getEta()));
            waypoints[count].setHeading(waypoint.getRouteLeg().getHeading().toString());
            waypoints[count].setLat(waypoint.getLatitude());
            waypoints[count].setLon(waypoint.getLongitude());
            count++;
        }

        request.setWaypoints(waypoints);

        logger.debug("Sending METOC request: {}", request);

        DmiSejlRuteService.SejlRuteResponse sejlRuteResponse = dmiSejlRuteService.sejlRute(request);

        logger.debug("Received METOC response: {}", sejlRuteResponse);

        // Filtering result such that metoc points are at least minimumMetocDistance a part
        if (sejlRuteResponse.getMetocForecast() != null && sejlRuteResponse.getMetocForecast().getForecasts() != null) {
            Forecast[] forecasts = sejlRuteResponse.getMetocForecast().getForecasts();
            ArrayList<Forecast> result = new ArrayList<>(forecasts.length);
            Position lastPosition = null;
            for (int i = 0; i < forecasts.length; i++) {
                Position position = Position.create(forecasts[i].getLat(), forecasts[i].getLon());
                if (lastPosition != null) {
                    // Using geodesic distance. Because the small distances in the comparison, geodesic vs rhumbline
                    // shouldnt matter
                    if (position.rhumbLineDistanceTo(lastPosition) > minimumMetocDistance) {
                        result.add(forecasts[i]);
                        lastPosition = position;
                    }
                } else {
                    result.add(forecasts[i]);
                    lastPosition = position;
                }
            }

            Forecast[] filtered = new Forecast[result.size()];
            filtered = result.toArray(filtered);
            sejlRuteResponse.getMetocForecast().setForecasts(filtered);
        }

        return sejlRuteResponse;
    }

}
