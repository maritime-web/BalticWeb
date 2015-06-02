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
package dk.dma.embryo.metoc.service;

import dk.dma.embryo.common.EmbryonicException;
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
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.ArrayList;

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
        
        dk.dma.enav.model.voyage.Route enavRoute = route.toEnavModel();
        enavRoute.getWaypoints().get(0).setEta(route.getVoyage().getDeparture().toDate());
        

        RouteDecorator r = new RouteDecorator(enavRoute);

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

            if (sejlRuteResponse.getError() != 0) {
                throw new EmbryonicException("METOC response contains error with code " + sejlRuteResponse.getError() + " and message '" + sejlRuteResponse.getErrorMsg() + "'");
            } else {
                logger.debug("Received METOC response: {}", sejlRuteResponse);
                int number = sejlRuteResponse.getMetocForecast() != null
                        && sejlRuteResponse.getMetocForecast().getForecasts() != null ? sejlRuteResponse.getMetocForecast()
                        .getForecasts().length : 0;
                logService.info("Received " + number + " forecasts from " + dmiSejlRuteServiceUrl);
            }

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
            logger.error("Error requesting METOC from {}", dmiSejlRuteServiceUrl, e);
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
