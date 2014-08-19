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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.security.auth.Subject;

import org.slf4j.Logger;

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.metoc.json.client.DmiSejlRuteService;
import dk.dma.embryo.metoc.json.client.DmiSejlRuteService.Forecast;
import dk.dma.embryo.metoc.json.client.DmiSejlRuteService.ForecastValue;
import dk.dma.embryo.metoc.json.client.DmiSejlRuteService.MetocForecast;
import dk.dma.embryo.metoc.json.client.DmiSejlRuteService.SejlRuteResponse;
import dk.dma.embryo.vessel.component.RouteDecorator;
import dk.dma.embryo.vessel.model.Route;
import dk.dma.embryo.vessel.persistence.VesselDao;
import dk.dma.enav.model.geometry.Position;
import dk.dma.enav.model.voyage.RouteLeg;
import dk.dma.enav.model.voyage.RouteLeg.Heading;
import dk.dma.enav.model.voyage.Waypoint;

public class MetocServiceMock /*implements MetocService */{

    @Inject
    private VesselDao vesselDao;

    @Inject
    private Logger logger;

    @Inject
    @Property("embryo.metoc.minDistance")
    private Integer minimumMetocDistance;

    private static double[] curDir = { 94, 123, 115, 120, 150, 180, 185, 184, 189, 220 };
    private static double[] curSpe = { 0.2, 0.5, 0.6, 0.4, 0.3, 0.5, 0.2, 0.1, 0.3, 0.2 };
    private static double[] winDir = { 10.0, 10.0, 11.0, 12.0, 16.0, 25.0, 24.0, 25.0, 28.0, 30.0 };
    private static double[] winSpe = { 6.1, 6.2, 6.5, 6.1, 6.1, 5.9, 5.5, 5.5, 5.6, 5.7 };
    private static double[] wavDir = { 10.0, 10.0, 11.0, 12.0, 16.0, 25.0, 24.0, 25.0, 28.0, 30.0 };
    private static double[] wavHei = { 1.5, 1.4, 1.5, 1.6, 1.8, 2.0, 1.8, 1.6, 1.5, 1.2 };
    private static double[] wavPer = { 5.23, 5.15, 5.10, 5.14, 4.90, 4.95, 4.80, 4.75, 4.60, 4.70, 4.86 };
    private static double[] seaLev = { 0.2, 0.1, 0.1, 0.0, -0.1, -0.2, -0.1, 0.0, 0.2, 0.3 };

    private static Forecast newForecast(Waypoint wp, int counter) {
        Forecast forecast = new Forecast();
        forecast.setLat(wp.getLatitude());
        forecast.setLon(wp.getLongitude());
        forecast.setTime(DmiSejlRuteService.DATE_FORMAT.format(wp.getEta()));
        forecast.setCurrentDir(newForecastValue(curDir[counter]));
        forecast.setCurrentSpeed(newForecastValue(curSpe[counter]));
        forecast.setWindDir(newForecastValue(winDir[counter]));
        forecast.setWindSpeed(newForecastValue(winSpe[counter]));
        forecast.setWaveDir(newForecastValue(wavDir[counter]));
        forecast.setWaveHeight(newForecastValue(wavHei[counter]));
        forecast.setWavePeriod(newForecastValue(wavPer[counter]));
        forecast.setSealevel(newForecastValue(seaLev[counter]));
        return forecast;
    }

    private static ForecastValue newForecastValue(double value) {
        ForecastValue forecast = new ForecastValue();
        forecast.setForecast(value);
        return forecast;
    }

    public MetocServiceMock() {
    }

    public MetocServiceMock(VesselDao vesselService, Subject subject) {
        this.vesselDao = vesselService;
    }

    //@Override
    public DmiSejlRuteService.SejlRuteResponse getMetoc(String routeId) {
        Route route = vesselDao.getRouteByEnavId(routeId);
        if (route == null) {
            throw new IllegalArgumentException("Unknown route id: " + routeId);
        }

        dk.dma.enav.model.voyage.Route enavRoute = route.toEnavModel();

        new RouteDecorator(enavRoute);

        List<Waypoint> waypoints = new ArrayList<>();

        for (int i = 0; i < enavRoute.getWaypoints().size() - 1; i++) {
            Waypoint curWp = enavRoute.getWaypoints().get(i);
            Waypoint nextWp = enavRoute.getWaypoints().get(i + 1);
            waypoints.add(curWp);

            double bearing = bearing(Position.create(curWp.getLatitude(), curWp.getLongitude()),
                    Position.create(nextWp.getLatitude(), nextWp.getLongitude()), curWp.getRouteLeg().getHeading());

            while (farApart(curWp, nextWp)) {

                curWp = createWaypoint(curWp, bearing);
                waypoints.add(curWp);
            }
        }
        

        List<Forecast> forecasts = new ArrayList<>();
        int counter = 0;
        for (Waypoint wp : waypoints) {
            forecasts.add(newForecast(wp, counter));

            counter++;
            if (counter == 10) {
                counter = 0;
            }
        }

        Forecast[] forecastsResult = new Forecast[forecasts.size()];
        forecastsResult = forecasts.toArray(forecastsResult);

        SejlRuteResponse sejlRuteResponse = new SejlRuteResponse();
        MetocForecast metocForecast = new MetocForecast();
        sejlRuteResponse.setMetocForecast(metocForecast);
        metocForecast.setForecasts(forecastsResult);

        return sejlRuteResponse;
    }

    private boolean farApart(Waypoint wp1, Waypoint wp2) {
        long dif = wp2.getEta().getTime() - wp1.getEta().getTime();
        
        return dif > 3600000;
    }

    private Waypoint createWaypoint(Waypoint wp, double bearing) {
        // km
        double earthRadius = 6371.0;
        double kmph = wp.getRouteLeg().getSpeed() * 1.852 ; //
        double distance = kmph * 1;

        double bearingRad = (bearing / 360) * 2 * Math.PI; 
        double latRad = (wp.getLatitude() / 360) * 2 * Math.PI; 
        double lonRad = (wp.getLongitude() / 360) * 2 * Math.PI; 
        
        double angularDistance = distance / earthRadius;

        double lat2 = Math.asin(Math.sin(latRad) * Math.cos(angularDistance) + (Math.cos(latRad)
                * Math.sin(angularDistance) * Math.cos(bearingRad)));

        double lon2 = lonRad
                + Math.atan2(Math.sin(bearingRad) * Math.sin(angularDistance) * Math.cos(latRad),
                        Math.cos(angularDistance) - Math.sin(latRad) * Math.sin(lat2));


        double latResult = (lat2 * 360) / (2 * Math.PI);
        double lonResult = (lon2 * 360) / (2 * Math.PI);
        
        logger.debug("lat2={}, lon2={} - ({},{})", lat2, lon2, latResult, lonResult);
        
        RouteLeg routeLeg = new RouteLeg(wp.getRouteLeg().getSpeed(), wp.getRouteLeg().getHeading(), wp.getRouteLeg()
                .getXtdPort(), wp.getRouteLeg().getXtdStarboard());
        Waypoint result = new Waypoint("generated", latResult, lonResult, wp.getRot(), wp.getTurnRad());
        result.setRouteLeg(routeLeg);
        result.setEta(new Date(wp.getEta().getTime() + 3600000));
        
        return result;
    }

    /**
     * Calculate bearing between two points given heading
     * 
     * @param pos1
     * @param pos2
     * @param heading
     * @return
     */
    private static double bearing(Position pos1, Position pos2, Heading heading) {
        if (heading == Heading.RL) {
            return pos1.rhumbLineBearingTo(pos2);
        } else {
            return pos1.geodesicInitialBearingTo(pos2);
        }

    }

}
