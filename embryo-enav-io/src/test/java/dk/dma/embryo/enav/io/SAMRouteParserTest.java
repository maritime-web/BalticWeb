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
package dk.dma.embryo.enav.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import dk.dma.enav.model.geometry.Position;
import dk.dma.enav.model.voyage.Route;
import dk.dma.enav.model.voyage.RouteLeg.Heading;
import dk.dma.enav.model.voyage.Waypoint;

/**
 * 
 * @author Jesper Tejlgaard
 */
public class SAMRouteParserTest {

    @Test
    public void testParseMap0137t() throws IOException {

        // Data
        Map<String, String> config = new HashMap<>();
        InputStream is = getClass().getResourceAsStream("/routes/map0137t");
        RouteParser parser = new SAMRouteParser(is, config);

        // Execute
        Route r = parser.parse();

        Assert.assertEquals("Iglorsuit-N-Uper14", r.getName());
        Assert.assertNull(r.getDeparture());
        Assert.assertNull(r.getDestination());

        Assert.assertEquals(15, r.getWaypoints().size());

        // Checking first waypoint
        Waypoint waypoint = r.getWaypoints().get(0);
        Position pos = Position.create(waypoint.getLatitude(), waypoint.getLongitude());
        Assert.assertEquals("0001", waypoint.getName());
        Assert.assertEquals("71 14.889N", pos.getLatitudeAsString());
        Assert.assertEquals("053 33.619W", pos.getLongitudeAsString());
        Assert.assertEquals(0.600, waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(0.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdStarboard(), 0.0);

        // Checking second waypoint
        waypoint = r.getWaypoints().get(1);
        pos = Position.create(waypoint.getLatitude(), waypoint.getLongitude());
        Assert.assertEquals("0002", waypoint.getName());
        Assert.assertEquals("71 16.458N", pos.getLatitudeAsString());
        Assert.assertEquals("053 30.995W", pos.getLongitudeAsString());

        Assert.assertEquals(0.600, waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(0.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdStarboard(), 0.0);

        // Checking third waypoint
        waypoint = r.getWaypoints().get(2);
        pos = Position.create(waypoint.getLatitude(), waypoint.getLongitude());
        Assert.assertEquals("0003", waypoint.getName());
        Assert.assertEquals("71 20.030N", pos.getLatitudeAsString());
        Assert.assertEquals("053 34.458W", pos.getLongitudeAsString());

        Assert.assertEquals(1.00, waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(0.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdStarboard(), 0.0);

        // Checking last waypoint
        waypoint = r.getWaypoints().get(r.getWaypoints().size() - 1);
        pos = Position.create(waypoint.getLatitude(), waypoint.getLongitude());
        Assert.assertEquals("Upernavik", waypoint.getName());
        Assert.assertEquals("72 47.332N", pos.getLatitudeAsString());
        Assert.assertEquals("056 08.830W", pos.getLongitudeAsString());

        Assert.assertEquals(0.30, waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(10.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdStarboard(), 0.0);
    }

    private void assertionsMap020xt(Route r, String title, Heading heading){
        Assert.assertEquals(title, r.getName());
        Assert.assertNull(r.getDeparture());
        Assert.assertNull(r.getDestination());

        Assert.assertEquals(25, r.getWaypoints().size());

        // Checking first waypoint
        Waypoint waypoint = r.getWaypoints().get(0);
        Position pos = Position.create(waypoint.getLatitude(), waypoint.getLongitude());
        Assert.assertEquals("SBB/RM BERTH No.7", waypoint.getName());
        Assert.assertEquals("59 56.694N", pos.getLatitudeAsString());
        Assert.assertEquals("030 10.986E", pos.getLongitudeAsString());
        Assert.assertEquals(0.30, waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(6.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(heading, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdStarboard(), 0.0);

        // Checking second waypoint
        waypoint = r.getWaypoints().get(1);
        pos = Position.create(waypoint.getLatitude(), waypoint.getLongitude());
        Assert.assertEquals("Buoy No.5&6", waypoint.getName());
        Assert.assertEquals("59 56.927N", pos.getLatitudeAsString());
        Assert.assertEquals("030 10.704E", pos.getLongitudeAsString());

        Assert.assertEquals(0.30, waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(10.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(heading, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdStarboard(), 0.0);

        // Checking third waypoint
        waypoint = r.getWaypoints().get(2);
        pos = Position.create(waypoint.getLatitude(), waypoint.getLongitude());
        Assert.assertEquals("Buoy No.1", waypoint.getName());
        Assert.assertEquals("59 57.135N", pos.getLatitudeAsString());
        Assert.assertEquals("030 08.567E", pos.getLongitudeAsString());

        Assert.assertEquals(0.50, waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(10.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(heading, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdStarboard(), 0.0);

        // Checking 10th waypoint
        waypoint = r.getWaypoints().get(9);
        pos = Position.create(waypoint.getLatitude(), waypoint.getLongitude());
        Assert.assertEquals("Buoy No.8", waypoint.getName());
        Assert.assertEquals("60 03.100N", pos.getLatitudeAsString());
        Assert.assertEquals("028 31.950E", pos.getLongitudeAsString());

        Assert.assertEquals(4.0, waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(24.0, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(heading, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdStarboard(), 0.0);

        
        // Checking second last waypoint
        waypoint = r.getWaypoints().get(r.getWaypoints().size() - 2);
        pos = Position.create(waypoint.getLatitude(), waypoint.getLongitude());
        Assert.assertEquals("APPROACH", waypoint.getName());
        Assert.assertEquals("59 27.904N", pos.getLatitudeAsString());
        Assert.assertEquals("024 46.004E", pos.getLongitudeAsString());

        Assert.assertEquals(0.50, waypoint.getTurnRad(), 0.00001);
        Assert.assertEquals(3.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(heading, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdStarboard(), 0.0);

        // Checking last waypoint
        waypoint = r.getWaypoints().get(r.getWaypoints().size() - 1);
        pos = Position.create(waypoint.getLatitude(), waypoint.getLongitude());
        Assert.assertEquals("PIER 27 SSTQ", waypoint.getName());
        Assert.assertEquals("59 26.997N", pos.getLatitudeAsString());
        Assert.assertEquals("024 46.030E", pos.getLongitudeAsString());

        Assert.assertEquals(0.50, waypoint.getTurnRad(), 0.00001);
        Assert.assertEquals(10.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(heading, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdStarboard(), 0.0);
    }
    
    @Test
    public void testParseMap0700t() throws IOException {
        // Data
        Map<String, String> config = new HashMap<>();
        InputStream is = getClass().getResourceAsStream("/routes/map0700t");
        RouteParser parser = new SAMRouteParser(is, config);
        // Execute
        Route r = parser.parse();

        assertionsMap020xt(r, "LED-TLL", Heading.RL);
    }
    @Test
    public void testParseMap0702t() throws IOException {
        // Data
        Map<String, String> config = new HashMap<>();
        InputStream is = getClass().getResourceAsStream("/routes/map0702t");
        RouteParser parser = new SAMRouteParser(is, config);
        // Execute
        Route r = parser.parse();

        assertionsMap020xt(r, "GREAT C. LED-TLL", Heading.GC);
    }
    
}
