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
public class RouteRouteParserTest {
    
    Map<String, String> config;

    @Before
    public void setup(){
        config = new HashMap<>();
    }

    /**
     * This route has been delivered by M/S Artania. Values have been verified against screen dump from their VisionMaster FT ECDIS. 
     *  
     * @throws IOException
     */
    @Test
    public void testParseSimpleRoute() throws IOException {
        // SETUP DATA        
        config.put("name", "Sisimiut - Nuuk");
        
        InputStream is = getClass().getResourceAsStream("/routes/Sisimiut - Nuuk.route");
        RouteParser parser = new RouteRouteParser(is, config);

        // EXECUTE
        Route r = parser.parse();

        // TEST ASSERTIONS
        Assert.assertEquals("Sisimiut - Nuuk", r.getName());
        Assert.assertEquals(17, r.getWaypoints().size());

        // TEST FIRST WAYPOINT
        Waypoint waypoint = r.getWaypoints().get(0);
        Assert.assertEquals("W1 Anchor Sisimiut", waypoint.getName());
        Assert.assertEquals(Math.toDegrees(1.1683225502151817), waypoint.getLatitude(), 0.0);
        Assert.assertEquals(Math.toDegrees(-0.93762267906239771), waypoint.getLongitude(), 0.0);
        Assert.assertEquals(966.82133492996763/(1.852*1000), waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(10, waypoint.getRouteLeg().getSpeed(), 0.1);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.10, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.10, waypoint.getRouteLeg().getXtdStarboard(), 0.0);

        // Checking second waypoint
        waypoint = r.getWaypoints().get(1);
        Assert.assertEquals("WP_002", waypoint.getName());
        Assert.assertEquals(Math.toDegrees(1.1684068671001553), waypoint.getLatitude(), 0.0);
        Assert.assertEquals(Math.toDegrees(-0.93804445291534289), waypoint.getLongitude(), 0.0);
        Assert.assertEquals(966.82133492996763/(1.852*1000), waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(10, waypoint.getRouteLeg().getSpeed(), 0.1);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.10, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.10, waypoint.getRouteLeg().getXtdStarboard(), 0.0);

        // Checking last waypoint
        waypoint = r.getWaypoints().get(r.getWaypoints().size() - 1);
        Assert.assertEquals("W17 Nuuk Pier", waypoint.getName());
        Assert.assertEquals(Math.toDegrees(1.1199542945689027), waypoint.getLatitude(), 0.0);
        Assert.assertEquals(Math.toDegrees(-0.90266832012447307), waypoint.getLongitude(), 0.0);
        Assert.assertEquals(530.84290107848619/(1.852*1000), waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(0.29, waypoint.getTurnRad(), 0.05);
        Assert.assertEquals(10, waypoint.getRouteLeg().getSpeed(), 0.1);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.10, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.10, waypoint.getRouteLeg().getXtdStarboard(), 0.0);
    }

    @Test
    public void testParseWithNames() throws IOException {
        // SETUP DATA        
        config.put("name", "Sisimiut - Nuuk, LONG, new");
        
        InputStream is = getClass().getResourceAsStream("/routes/SISIMIUT - NUUK - 2Routes.ROUTE");
        RouteParser parser = new RouteRouteParser(is, config);

        // EXECUTE
        Route r = parser.parse();

        // TEST ASSERTIONS
        Assert.assertEquals("Sisimiut - Nuuk, LONG, new", r.getName());
        Assert.assertEquals(17, r.getWaypoints().size());

        // TEST FIRST WAYPOINT
        Waypoint waypoint = r.getWaypoints().get(0);
        Assert.assertEquals("W1 Anchor Sisimiut", waypoint.getName());
        Assert.assertEquals(Math.toDegrees(1.1683225502151817), waypoint.getLatitude(), 0.0);
        Assert.assertEquals(Math.toDegrees(-0.93762267906239771), waypoint.getLongitude(), 0.0);
        Assert.assertEquals(966.82133492996763/(1.852*1000), waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(0.52, waypoint.getTurnRad(), 0.05);
        Assert.assertEquals(10, waypoint.getRouteLeg().getSpeed(), 0.1);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.10, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.10, waypoint.getRouteLeg().getXtdStarboard(), 0.0);

        // Checking second waypoint
        waypoint = r.getWaypoints().get(1);
        Assert.assertEquals("WP_002", waypoint.getName());
        Assert.assertEquals(Math.toDegrees(1.1684068671001553), waypoint.getLatitude(), 0.0);
        Assert.assertEquals(Math.toDegrees(-0.93804445291534289), waypoint.getLongitude(), 0.0);
        Assert.assertEquals(966.82133492996763/(1.852*1000), waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(0.52, waypoint.getTurnRad(), 0.05);
        Assert.assertEquals(10, waypoint.getRouteLeg().getSpeed(), 0.1);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.10, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.10, waypoint.getRouteLeg().getXtdStarboard(), 0.0);

        // Checking last waypoint
        waypoint = r.getWaypoints().get(r.getWaypoints().size() - 1);
        Assert.assertEquals("W17 Nuuk Pier", waypoint.getName());
        Assert.assertEquals(Math.toDegrees(1.1199542945689027), waypoint.getLatitude(), 0.0);
        Assert.assertEquals(Math.toDegrees(-0.90266832012447307), waypoint.getLongitude(), 0.0);
        Assert.assertEquals(530.84290107848619/(1.852*1000), waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(0.29, waypoint.getTurnRad(), 0.05);
        Assert.assertEquals(10, waypoint.getRouteLeg().getSpeed(), 0.1);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.10, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.10, waypoint.getRouteLeg().getXtdStarboard(), 0.0);
    }

}
