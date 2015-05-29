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
import dk.dma.embryo.common.configuration.LogConfiguration;
import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.common.log.EmbryoLogService;
import dk.dma.embryo.metoc.json.client.DmiSejlRuteService;
import dk.dma.embryo.metoc.json.client.DmiSejlRuteService.Forecast;
import dk.dma.embryo.metoc.json.client.DmiSejlRuteService.MetocForecast;
import dk.dma.embryo.metoc.json.client.DmiSejlRuteService.Waypoint;
import dk.dma.embryo.vessel.model.Route;
import dk.dma.embryo.vessel.model.RouteLeg;
import dk.dma.embryo.vessel.model.Vessel;
import dk.dma.embryo.vessel.model.Voyage;
import dk.dma.embryo.vessel.model.WayPoint;
import dk.dma.embryo.vessel.persistence.VesselDao;
import dk.dma.enav.model.voyage.RouteLeg.Heading;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * @author Jesper Tejlgaard
 */

@RunWith(CdiRunner.class)
@AdditionalClasses({ PropertyFileService.class, LogConfiguration.class, MetocServiceImpl.class })
public class MetocServiceImplTest {

    @Inject
    private MetocService metocService;

    @Mock
    @Produces
    private DmiSejlRuteService dmiSejlRuteService;

    @Mock
    @Produces
    private VesselDao vesselDao;

    @Mock
    @Produces
    private EmbryoLogService logService;

    @Test
    public void testEmbryoLogging() {
        
        Vessel vessel = new Vessel(1234L);

        Voyage voyage = new Voyage("voyageId");
        vessel.addVoyageEntry(voyage);

        DateTime departureDate = DateTime.parse("2014-07-11T10:00:00.000+0000");
        voyage.setDeparture(departureDate);
        String routeId = "routeId";
        Route route = new Route(routeId, "route", "start", "end");
        route.addWayPoint(new WayPoint("001", 0.1, 0.1, 1.0, 0.5));
        route.getWayPoints().get(0).setLeg(new RouteLeg(10.0, 1.0, 1.0, Heading.RL));
        route.addWayPoint(new WayPoint("002", 0.1, 0.1, 1.0, 0.5));
        route.getWayPoints().get(1).setLeg(new RouteLeg(10.0, 1.0, 1.0, Heading.RL));
        route.setVoyage(voyage);

        DmiSejlRuteService.SejlRuteRequest sejlRuteRequest = new DmiSejlRuteService.SejlRuteRequest();
        sejlRuteRequest.setMssi(vessel.getMmsi());
        sejlRuteRequest.setDatatypes(new String[] { "sealevel", "current", "wave", "wind", "density" });
        sejlRuteRequest.setDt(60);

        
        DmiSejlRuteService.Waypoint[] waypoints = new DmiSejlRuteService.Waypoint[2];
        waypoints[0] = new Waypoint();
        waypoints[0].setEta(DmiSejlRuteService.DATE_FORMAT.format(departureDate.toDate()));
        waypoints[0].setHeading("RL");
        waypoints[0].setLat(0.1);
        waypoints[0].setLon(0.1);
        waypoints[1] = new Waypoint();
        waypoints[1].setEta(DmiSejlRuteService.DATE_FORMAT.format(departureDate.toDate()));
        waypoints[1].setHeading("RL");
        waypoints[1].setLat(0.1);
        waypoints[1].setLon(0.1);
        sejlRuteRequest.setWaypoints(waypoints);
        
        DmiSejlRuteService.SejlRuteResponse sejlRuteResponse = new DmiSejlRuteService.SejlRuteResponse();
        sejlRuteResponse.setMetocForecast(new MetocForecast());
        sejlRuteResponse.getMetocForecast().setForecasts(new Forecast[0]);

        Mockito.when(vesselDao.getRouteByEnavId(routeId)).thenReturn(route);

        Mockito.when(dmiSejlRuteService.sejlRute(sejlRuteRequest)).thenReturn(sejlRuteResponse);
        
        sejlRuteResponse = metocService.getMetoc(routeId);

        Mockito.verify(logService).info("Received 0 forecasts from http://sejlrute.dmi.dk/SejlRute");
    }

    @Test
    public void testResponseWithError() {

        Vessel vessel = new Vessel(1234L);

        Voyage voyage = new Voyage("voyageId");
        vessel.addVoyageEntry(voyage);

        DateTime departureDate = DateTime.parse("2014-07-11T10:00:00.000+0000");
        voyage.setDeparture(departureDate);
        String routeId = "routeId";
        Route route = new Route(routeId, "route", "start", "end");
        route.addWayPoint(new WayPoint("001", 0.1, 0.1, 1.0, 0.5));
        route.getWayPoints().get(0).setLeg(new RouteLeg(10.0, 1.0, 1.0, Heading.RL));
        route.addWayPoint(new WayPoint("002", 0.1, 0.1, 1.0, 0.5));
        route.getWayPoints().get(1).setLeg(new RouteLeg(10.0, 1.0, 1.0, Heading.RL));
        route.setVoyage(voyage);

        DmiSejlRuteService.SejlRuteRequest sejlRuteRequest = new DmiSejlRuteService.SejlRuteRequest();
        sejlRuteRequest.setMssi(vessel.getMmsi());
        sejlRuteRequest.setDatatypes(new String[]{"sealevel", "current", "wave", "wind", "density"});
        sejlRuteRequest.setDt(60);

        DmiSejlRuteService.Waypoint[] waypoints = new DmiSejlRuteService.Waypoint[2];
        waypoints[0] = new Waypoint();
        waypoints[0].setEta(DmiSejlRuteService.DATE_FORMAT.format(departureDate.toDate()));
        waypoints[0].setHeading("RL");
        waypoints[0].setLat(0.1);
        waypoints[0].setLon(0.1);
        waypoints[1] = new Waypoint();
        waypoints[1].setEta(DmiSejlRuteService.DATE_FORMAT.format(departureDate.toDate()));
        waypoints[1].setHeading("RL");
        waypoints[1].setLat(0.1);
        waypoints[1].setLon(0.1);
        sejlRuteRequest.setWaypoints(waypoints);

        DmiSejlRuteService.SejlRuteResponse sejlRuteResponse = new DmiSejlRuteService.SejlRuteResponse();
        sejlRuteResponse.setError(9);
        sejlRuteResponse.setErrorMsg("Error. Sorry!");
        sejlRuteResponse.setMetocForecast(new MetocForecast());
        sejlRuteResponse.getMetocForecast().setForecasts(new Forecast[0]);

        Mockito.when(vesselDao.getRouteByEnavId(routeId)).thenReturn(route);

        Mockito.when(dmiSejlRuteService.sejlRute(sejlRuteRequest)).thenReturn(sejlRuteResponse);

        try {
            metocService.getMetoc(routeId);
            Assert.fail("Exception expected!");
        } catch (EmbryonicException e) {
            Assert.assertEquals("METOC response contains error with code 9 and message 'Error. Sorry!'", e.getMessage());
            Mockito.verify(logService).error("Error requesting METOC from http://sejlrute.dmi.dk/SejlRute", e);
        }
    }
}
