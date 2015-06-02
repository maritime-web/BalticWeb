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

import static dk.dma.embryo.metoc.json.client.DmiSejlRuteService.SejlRuteRequest;
import static dk.dma.embryo.metoc.json.client.DmiSejlRuteService.SejlRuteResponse;

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


    public Route createTestRouteWithVoyageAndVessel() {
        Vessel vessel = new Vessel(1234L);

        Voyage voyage = new Voyage("voyageId");
        vessel.addVoyageEntry(voyage);
        DateTime departureDate = DateTime.parse("2014-07-11T10:00:00.000+0000");
        voyage.setDeparture(departureDate);

        Route route = new Route("routeId", "route", "start", "end");
        route.addWayPoint(new WayPoint("001", 0.1, 0.1, 1.0, 0.5));
        route.getWayPoints().get(0).setLeg(new RouteLeg(10.0, 1.0, 1.0, Heading.RL));
        route.addWayPoint(new WayPoint("002", 0.1, 0.1, 1.0, 0.5));
        route.getWayPoints().get(1).setLeg(new RouteLeg(10.0, 1.0, 1.0, Heading.RL));
        route.setVoyage(voyage);
        return route;
    }

    public DmiSejlRuteService.SejlRuteRequest createExpectetSejlruteRequest(Route route) {
        DateTime departureDate = route.getVoyage().getDeparture();
        Long mmsi = route.getVoyage().getVessel().getMmsi();

        DmiSejlRuteService.SejlRuteRequest sejlRuteRequest = new DmiSejlRuteService.SejlRuteRequest();
        sejlRuteRequest.setMssi(mmsi);
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

        return sejlRuteRequest;
    }

    public SejlRuteResponse createSejlruteErrorResponse(int errorCode, String errorMsg) {
        SejlRuteResponse sejlRuteResponse = new DmiSejlRuteService.SejlRuteResponse();
        sejlRuteResponse.setError(errorCode);
        sejlRuteResponse.setErrorMsg(errorMsg);
        sejlRuteResponse.setMetocForecast(new MetocForecast());
        sejlRuteResponse.getMetocForecast().setForecasts(new Forecast[0]);
        return sejlRuteResponse;
    }

    @Test
    public void testEmbryoLogging() {

        Route route = createTestRouteWithVoyageAndVessel();
        SejlRuteRequest request = createExpectetSejlruteRequest(route);
        String routeId = route.getEnavId();

        DmiSejlRuteService.SejlRuteResponse sejlRuteResponse = new DmiSejlRuteService.SejlRuteResponse();
        sejlRuteResponse.setMetocForecast(new MetocForecast());
        sejlRuteResponse.getMetocForecast().setForecasts(new Forecast[0]);

        Mockito.when(vesselDao.getRouteByEnavId(routeId)).thenReturn(route);

        Mockito.when(dmiSejlRuteService.sejlRute(request)).thenReturn(sejlRuteResponse);

        sejlRuteResponse = metocService.getMetoc(routeId);

        Mockito.verify(logService).info("Received 0 forecasts from http://sejlrute.dmi.dk/SejlRute");
    }

    @Test
    public void testResponseWithError1() {
        Route route = createTestRouteWithVoyageAndVessel();
        SejlRuteRequest sejlRuteRequest = createExpectetSejlruteRequest(route);
        SejlRuteResponse sejlRuteResponse = createSejlruteErrorResponse(1, "Too few waypoints");

        String routeId = route.getEnavId();
        Mockito.when(vesselDao.getRouteByEnavId(routeId)).thenReturn(route);

        Mockito.when(dmiSejlRuteService.sejlRute(sejlRuteRequest)).thenReturn(sejlRuteResponse);

        try {
            metocService.getMetoc(routeId);
            Assert.fail("Exception expected!");
        } catch (EmbryonicException e) {
            Assert.assertEquals("METOC response contains error with code 1 and message 'Too few waypoints'", e.getMessage());
            Mockito.verify(logService).error("Error requesting METOC from http://sejlrute.dmi.dk/SejlRute", e);
        }
    }

    @Test
    public void testResponseWithError2() {
        Route route = createTestRouteWithVoyageAndVessel();
        SejlRuteRequest sejlRuteRequest = createExpectetSejlruteRequest(route);
        SejlRuteResponse sejlRuteResponse = createSejlruteErrorResponse(2, "Invalid parameter");

        String routeId = route.getEnavId();
        Mockito.when(vesselDao.getRouteByEnavId(routeId)).thenReturn(route);

        Mockito.when(dmiSejlRuteService.sejlRute(sejlRuteRequest)).thenReturn(sejlRuteResponse);

        try {
            metocService.getMetoc(routeId);
            Assert.fail("Exception expected!");
        } catch (EmbryonicException e) {
            Assert.assertEquals("METOC response contains error with code 2 and message 'Invalid parameter'", e.getMessage());
            Mockito.verify(logService).error("Error requesting METOC from http://sejlrute.dmi.dk/SejlRute", e);
        }
    }

    @Test
    public void testResponseWithError3() {
        Route route = createTestRouteWithVoyageAndVessel();
        SejlRuteRequest sejlRuteRequest = createExpectetSejlruteRequest(route);
        SejlRuteResponse sejlRuteResponse = createSejlruteErrorResponse(3, "Invalid direction");

        String routeId = route.getEnavId();
        Mockito.when(vesselDao.getRouteByEnavId(routeId)).thenReturn(route);

        Mockito.when(dmiSejlRuteService.sejlRute(sejlRuteRequest)).thenReturn(sejlRuteResponse);

        try {
            metocService.getMetoc(routeId);
            Assert.fail("Exception expected!");
        } catch (EmbryonicException e) {
            Assert.assertEquals("METOC response contains error with code 3 and message 'Invalid direction'", e.getMessage());
            Mockito.verify(logService).error("Error requesting METOC from http://sejlrute.dmi.dk/SejlRute", e);
        }
    }

    @Test
    public void testResponseWithError5() {
        Route route = createTestRouteWithVoyageAndVessel();
        SejlRuteRequest sejlRuteRequest = createExpectetSejlruteRequest(route);
        SejlRuteResponse sejlRuteResponse = createSejlruteErrorResponse(5, "Missing or invalid dates");

        String routeId = route.getEnavId();
        Mockito.when(vesselDao.getRouteByEnavId(routeId)).thenReturn(route);

        Mockito.when(dmiSejlRuteService.sejlRute(sejlRuteRequest)).thenReturn(sejlRuteResponse);

        try {
            metocService.getMetoc(routeId);
            Assert.fail("Exception expected!");
        } catch (EmbryonicException e) {
            Assert.assertEquals("METOC response contains error with code 5 and message 'Missing or invalid dates'", e.getMessage());
            Mockito.verify(logService).error("Error requesting METOC from http://sejlrute.dmi.dk/SejlRute", e);
        }
    }

    @Test
    public void testResponseWithError6() {
        Route route = createTestRouteWithVoyageAndVessel();
        SejlRuteRequest sejlRuteRequest = createExpectetSejlruteRequest(route);
        SejlRuteResponse sejlRuteResponse = createSejlruteErrorResponse(6, "Invalid path");

        String routeId = route.getEnavId();
        Mockito.when(vesselDao.getRouteByEnavId(routeId)).thenReturn(route);

        Mockito.when(dmiSejlRuteService.sejlRute(sejlRuteRequest)).thenReturn(sejlRuteResponse);

        try {
            metocService.getMetoc(routeId);
            Assert.fail("Exception expected!");
        } catch (EmbryonicException e) {
            Assert.assertEquals("METOC response contains error with code 6 and message 'Invalid path'", e.getMessage());
            Mockito.verify(logService).error("Error requesting METOC from http://sejlrute.dmi.dk/SejlRute", e);
        }
    }

    @Test
    public void testResponseWithError7() {
        Route route = createTestRouteWithVoyageAndVessel();
        SejlRuteRequest sejlRuteRequest = createExpectetSejlruteRequest(route);
        SejlRuteResponse sejlRuteResponse = createSejlruteErrorResponse(7, "Invalid dates");

        String routeId = route.getEnavId();
        Mockito.when(vesselDao.getRouteByEnavId(routeId)).thenReturn(route);

        Mockito.when(dmiSejlRuteService.sejlRute(sejlRuteRequest)).thenReturn(sejlRuteResponse);

        try {
            metocService.getMetoc(routeId);
            Assert.fail("Exception expected!");
        } catch (EmbryonicException e) {
            Assert.assertEquals("METOC response contains error with code 7 and message 'Invalid dates'", e.getMessage());
            Mockito.verify(logService).error("Error requesting METOC from http://sejlrute.dmi.dk/SejlRute", e);
        }
    }

    @Test
    public void testResponseWithError8() {
        Route route = createTestRouteWithVoyageAndVessel();
        SejlRuteRequest sejlRuteRequest = createExpectetSejlruteRequest(route);
        SejlRuteResponse sejlRuteResponse = createSejlruteErrorResponse(8, "Missing or invalid datatypes");

        String routeId = route.getEnavId();
        Mockito.when(vesselDao.getRouteByEnavId(routeId)).thenReturn(route);

        Mockito.when(dmiSejlRuteService.sejlRute(sejlRuteRequest)).thenReturn(sejlRuteResponse);

        try {
            metocService.getMetoc(routeId);
            Assert.fail("Exception expected!");
        } catch (EmbryonicException e) {
            Assert.assertEquals("METOC response contains error with code 8 and message 'Missing or invalid datatypes'", e.getMessage());
            Mockito.verify(logService).error("Error requesting METOC from http://sejlrute.dmi.dk/SejlRute", e);
        }
    }

    @Test
    public void testResponseWithError9() {
        Route route = createTestRouteWithVoyageAndVessel();
        SejlRuteRequest sejlRuteRequest = createExpectetSejlruteRequest(route);
        SejlRuteResponse sejlRuteResponse = createSejlruteErrorResponse(9, "Error. Sorry!");

        String routeId = route.getEnavId();
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

    @Test
    public void testResponseWithError10() {
        Route route = createTestRouteWithVoyageAndVessel();
        SejlRuteRequest sejlRuteRequest = createExpectetSejlruteRequest(route);
        SejlRuteResponse sejlRuteResponse = createSejlruteErrorResponse(10, "Problem with delta-T (dt)");

        String routeId = route.getEnavId();
        Mockito.when(vesselDao.getRouteByEnavId(routeId)).thenReturn(route);

        Mockito.when(dmiSejlRuteService.sejlRute(sejlRuteRequest)).thenReturn(sejlRuteResponse);

        try {
            metocService.getMetoc(routeId);
            Assert.fail("Exception expected!");
        } catch (EmbryonicException e) {
            Assert.assertEquals("METOC response contains error with code 10 and message 'Problem with delta-T (dt)'", e.getMessage());
            Mockito.verify(logService).error("Error requesting METOC from http://sejlrute.dmi.dk/SejlRute", e);
        }
    }
}
