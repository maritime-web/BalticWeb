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
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import dk.dma.arcticweb.dao.ShipDao;
import dk.dma.commons.model.Converter;
import dk.dma.commons.model.RouteDecorator;
import dk.dma.configuration.Property;
import dk.dma.embryo.domain.Route;
import dk.dma.embryo.restclients.DmiSejlRuteService;
import dk.dma.embryo.restclients.DmiSejlRuteService.Forecast;
import dk.dma.embryo.restclients.DmiSejlRuteService.ForecastValue;
import dk.dma.embryo.restclients.DmiSejlRuteService.MetocForecast;
import dk.dma.embryo.restclients.DmiSejlRuteService.SejlRuteResponse;
import dk.dma.embryo.security.Subject;
import dk.dma.enav.model.geometry.CoordinateSystem;
import dk.dma.enav.model.geometry.Position;
import dk.dma.enav.model.voyage.RouteLeg;
import dk.dma.enav.model.voyage.RouteLeg.Heading;
import dk.dma.enav.model.voyage.Waypoint;

@Stateless
public class MetocServiceMock implements MetocService {

    @Inject
    private Subject subject;

    @Inject
    private ShipDao shipDao;

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

    public MetocServiceMock(ShipDao shipDao, Subject subject) {
        this.shipDao = shipDao;
        this.subject = subject;
    }

    @Override
    public DmiSejlRuteService.SejlRuteResponse getMetoc(String routeId) {
        Route route = shipDao.getRouteByEnavId(routeId);
        if (route == null) {
            throw new IllegalArgumentException("Unknown route id: " + routeId);
        }

        dk.dma.enav.model.voyage.Route enavRoute = route.toEnavModel();

        RouteDecorator r = new RouteDecorator(enavRoute);

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
        
        System.out.println(dif);
        
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

        System.out.println("distance=" + distance + ", bearing=" + bearing + ", lat=" + wp.getLatitude()+ ", lon=" + wp.getLongitude());

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