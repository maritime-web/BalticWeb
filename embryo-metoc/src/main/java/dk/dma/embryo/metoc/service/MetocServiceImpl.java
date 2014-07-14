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
package dk.dma.embryo.metoc.service;

import java.util.ArrayList;

import javax.inject.Inject;

import org.slf4j.Logger;

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.log.EmbryoLogService;
import dk.dma.embryo.metoc.json.client.DmiSejlRuteService;
import dk.dma.embryo.metoc.json.client.DmiSejlRuteService.Forecast;
import dk.dma.embryo.metoc.json.client.DmiSejlRuteService.Waypoint;
import dk.dma.embryo.vessel.component.RouteDecorator;
import dk.dma.embryo.vessel.model.Route;
import dk.dma.embryo.vessel.model.Vessel;
import dk.dma.embryo.vessel.persistence.VesselDao;
import dk.dma.enav.model.geometry.Position;

public class MetocServiceImpl implements MetocService {

    @Inject
    private DmiSejlRuteService dmiSejlRuteService;

    @Inject
    private VesselDao vesselDao;

    @Inject
    private EmbryoLogService logService;

    @Inject
    private Logger logger;

    @Inject
    @Property("embryo.metoc.minDistance")
    private Integer minimumMetocDistance;

    @Inject
    @Property("dk.dma.embryo.restclients.dmiSejlRuteServiceUrl")
    private String dmiSejlRuteServiceUrl;

    public MetocServiceImpl() {
    }

    public MetocServiceImpl(VesselDao vesselDao) {
        this.vesselDao = vesselDao;
    }

    @Override
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

        try {
            logger.debug("Sending METOC request: {}", request);

            DmiSejlRuteService.SejlRuteResponse sejlRuteResponse = dmiSejlRuteService.sejlRute(request);

            logger.debug("Received METOC response: {}", sejlRuteResponse);
            int number = sejlRuteResponse.getMetocForecast() != null
                    && sejlRuteResponse.getMetocForecast().getForecasts() != null ? sejlRuteResponse.getMetocForecast()
                    .getForecasts().length : 0;
                    
            logService.info("Received " + number + " forecasts from " + dmiSejlRuteServiceUrl);

            // Filtering result such that metoc points are at least minimumMetocDistance a part
            if (sejlRuteResponse.getMetocForecast() != null
                    && sejlRuteResponse.getMetocForecast().getForecasts() != null) {
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
        } catch (Exception e) {
            logService.error("Error requesting METOC from " + dmiSejlRuteServiceUrl, e);
            throw e;
        }

    }

    public DmiSejlRuteService.SejlRuteResponse[] listMetocs(String[] routeIds) {
        DmiSejlRuteService.SejlRuteResponse[] metocs = new DmiSejlRuteService.SejlRuteResponse[routeIds.length];

        for (int i = 0; i < metocs.length; i++) {
            metocs[i] = getMetoc(routeIds[i]);
        }

        return metocs;
    }

}
