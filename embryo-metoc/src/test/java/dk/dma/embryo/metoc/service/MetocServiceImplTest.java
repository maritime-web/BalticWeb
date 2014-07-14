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

import java.util.Arrays;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

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
        String routeId = "routeId";
        Route route = new Route(routeId, "route", "start", "end");
        route.setEtaOfDeparture(departureDate);
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
}
