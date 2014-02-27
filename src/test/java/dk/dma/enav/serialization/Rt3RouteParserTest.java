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
package dk.dma.enav.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dk.dma.enav.model.voyage.Route;
import dk.dma.enav.model.voyage.RouteLeg.Heading;
import dk.dma.enav.model.voyage.Waypoint;

/**
 * 
 * @author Jesper Tejlgaard
 */
public class Rt3RouteParserTest {
    
    Map<String, String> config;

    @Before
    public void setup(){
        config = new HashMap<>();
    }

    @Test
    public void testParseValidStream() throws IOException {
        // SETUP DATA
        
        InputStream is = getClass().getResourceAsStream("/routes/Malmoe - Karlshavn.rt3");
        RouteParser parser = new Rt3RouteParser(is, config);

        // EXECUTE
        Route r = parser.parse();

        // TEST ASSERTIONS
        Assert.assertEquals("Malmoe - Karlshavn", r.getName());
        Assert.assertEquals(14, r.getWaypoints().size());

        // TEST FIRST WAYPOINT
        Waypoint waypoint = r.getWaypoints().get(0);
        Assert.assertEquals("WP_001", waypoint.getName());
        Assert.assertEquals(3337.749 / 60, waypoint.getLatitude(), 0.0);
        Assert.assertEquals(778.673/60, waypoint.getLongitude(), 0.0);
        Assert.assertEquals(0.300000011920929, waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(10.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.100000001490116, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.100000001490116, waypoint.getRouteLeg().getXtdStarboard(), 0.0);

        // Checking second waypoint
        waypoint = r.getWaypoints().get(1);
        Assert.assertEquals("WP_002", waypoint.getName());
        Assert.assertEquals(3337.14796946844 / 60, waypoint.getLatitude(), 0.0);
        Assert.assertEquals(773.621608642612/60, waypoint.getLongitude(), 0.0);
        Assert.assertEquals(0.300000011920929, waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(10.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.100000001490116, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.100000001490116, waypoint.getRouteLeg().getXtdStarboard(), 0.0);

        // Checking last waypoint
        waypoint = r.getWaypoints().get(r.getWaypoints().size() - 1);
        Assert.assertEquals("WP_014", waypoint.getName());
        Assert.assertEquals(3369.34 / 60, waypoint.getLatitude(), 0.0);
        Assert.assertEquals(891.843/60, waypoint.getLongitude(), 0.0);
        Assert.assertEquals(0.300000011920929, waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(10.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.100000001490116, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.100000001490116, waypoint.getRouteLeg().getXtdStarboard(), 0.0);
    }

    @Test
    public void testParseWithNames() throws IOException {
        // SETUP DATA
        config.put("name", "Schedule1");
        InputStream is = getClass().getResourceAsStream("/routes/Nuuk-Paamiut 008.rt3");
        RouteParser parser = new Rt3RouteParser(is, config);

        // EXECUTE
        Route r = parser.parse();

        // TEST ASSERTIONS
        Assert.assertEquals("Nuuk-Paamiut 008", r.getName());
        Assert.assertEquals(20, r.getWaypoints().size());

        // TEST FIRST WAYPOINT
        Waypoint waypoint = r.getWaypoints().get(0);
        Assert.assertEquals("Nuuk Havn", waypoint.getName());
        // Assert.assertEquals("55 42.510N", Position.create(waypoint.getLatitude(), 0).getLatitudeAsString());
        // Assert.assertEquals("012 36.724E", Position.create(0, waypoint.getLongitude()).getLongitudeAsString());
        Assert.assertEquals(0.0, waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(0.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.100000, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.100000, waypoint.getRouteLeg().getXtdStarboard(), 0.0);

        // TEST SECOND WAYPOINT
        waypoint = r.getWaypoints().get(1);
        Assert.assertEquals("Anduvning Nuuk", waypoint.getName());
        // Assert.assertEquals("55 42.510N", Position.create(waypoint.getLatitude(), 0).getLatitudeAsString());
        // Assert.assertEquals("012 36.724E", Position.create(0, waypoint.getLongitude()).getLongitudeAsString());
        Assert.assertEquals(0.0, waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(0.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.100000, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.100000, waypoint.getRouteLeg().getXtdStarboard(), 0.0);

        // TEST SIXTH WAYPOINT (without name)
        waypoint = r.getWaypoints().get(5);
        Assert.assertEquals("WP_006", waypoint.getName());
        // Assert.assertEquals("55 42.510N", Position.create(waypoint.getLatitude(), 0).getLatitudeAsString());
        // Assert.assertEquals("012 36.724E", Position.create(0, waypoint.getLongitude()).getLongitudeAsString());
        Assert.assertEquals(0.0, waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(0.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.100000, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.100000, waypoint.getRouteLeg().getXtdStarboard(), 0.0);

        // TEST LAST WAYPOINT
        waypoint = r.getWaypoints().get(r.getWaypoints().size() - 1);
        Assert.assertEquals("Paamiut havn", waypoint.getName());
        // Assert.assertEquals("55 42.510N", Position.create(waypoint.getLatitude(), 0).getLatitudeAsString());
        // Assert.assertEquals("012 36.724E", Position.create(0, waypoint.getLongitude()).getLongitudeAsString());
        Assert.assertEquals(0.0, waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(0.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.100000, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.100000, waypoint.getRouteLeg().getXtdStarboard(), 0.0);
    }

    @Test
    public void testParseWithSpeeds() throws IOException {
        // SETUP DATA
        config.put("name", "Dep 1930");
        InputStream is = getClass().getResourceAsStream("/routes/GOT to KIEL via South Channel 1,0N.RT3");
        // EXECUTE
        RouteParser parser = new Rt3RouteParser(is, config);
        Route r = parser.parse();


        // TEST ASSERTIONS
        Assert.assertEquals("GOT to KIEL via South Channel 1,0N", r.getName());
        Assert.assertEquals(56, r.getWaypoints().size());

        // TEST FIRST WAYPOINT
        Waypoint waypoint = r.getWaypoints().get(0);
        Assert.assertEquals("Majnabbe", waypoint.getName());
        Assert.assertEquals(3461.627474 / 60, waypoint.getLatitude(), 0.0);
        Assert.assertEquals(714.459258/60, waypoint.getLongitude(), 0.0);
        Assert.assertEquals(0.0, waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(00.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.0, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.0, waypoint.getRouteLeg().getXtdStarboard(), 0.0);

        // TEST SECOND WAYPOINT
        waypoint = r.getWaypoints().get(1);
        Assert.assertEquals("Alvsborgsbron", waypoint.getName());
        Assert.assertEquals(3461.459000 / 60, waypoint.getLatitude(), 0.0);
        Assert.assertEquals(714.090000/60, waypoint.getLongitude(), 0.0);
        Assert.assertEquals(1.0, waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(3.0, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.034064, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.036514, waypoint.getRouteLeg().getXtdStarboard(), 0.0);

        // TEST LAST WAYPOINT
        waypoint = r.getWaypoints().get(r.getWaypoints().size() - 1);
        Assert.assertEquals("Kiel", waypoint.getName());
        Assert.assertEquals(3259.078728 / 60, waypoint.getLatitude(), 0.0);
        Assert.assertEquals(608.216024/60, waypoint.getLongitude(), 0.0);
        Assert.assertEquals(0.0, waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(1.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.020000, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.020000, waypoint.getRouteLeg().getXtdStarboard(), 0.0);
    }

}
