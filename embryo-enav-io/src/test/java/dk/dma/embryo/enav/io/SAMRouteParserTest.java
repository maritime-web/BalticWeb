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
    public void test() {
        // System.out.println(1389 / 1.78);
        // System.out.println(11112 / 5.52);
        // System.out.println(5556 / 11.56);
        // System.out.println(11112 / 23.17);

        System.out.println(463 / 7.04);
        System.out.println(7.04 / 463);

    }

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
        Assert.assertEquals("001", waypoint.getName());
        Assert.assertEquals("71 14.889N", pos.getLatitudeAsString());
        Assert.assertEquals("053 33.619W", pos.getLongitudeAsString());
        Assert.assertEquals(0.600, waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(10.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdStarboard(), 0.0);

        // Checking second waypoint
        waypoint = r.getWaypoints().get(1);
        pos = Position.create(waypoint.getLatitude(), waypoint.getLongitude());
        Assert.assertEquals("002", waypoint.getName());
        Assert.assertEquals("71 16.458N", pos.getLatitudeAsString());
        Assert.assertEquals("053 30.995W", pos.getLongitudeAsString());

        Assert.assertEquals(0.600, waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(10.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdStarboard(), 0.0);

        // Checking third waypoint
        waypoint = r.getWaypoints().get(2);
        pos = Position.create(waypoint.getLatitude(), waypoint.getLongitude());
        Assert.assertEquals("003", waypoint.getName());
        Assert.assertEquals("71 20.030N", pos.getLatitudeAsString());
        Assert.assertEquals("053 34.458W", pos.getLongitudeAsString());

        Assert.assertEquals(1.00, waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(10.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdStarboard(), 0.0);

        // Checking last waypoint
        waypoint = r.getWaypoints().get(r.getWaypoints().size() - 1);
        pos = Position.create(waypoint.getLatitude(), waypoint.getLongitude());
        Assert.assertEquals("015", waypoint.getName());
        Assert.assertEquals("72 47.332N", pos.getLatitudeAsString());
        Assert.assertEquals("056 08.830W", pos.getLongitudeAsString());

        Assert.assertEquals(0.30, waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(10.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdStarboard(), 0.0);
    }

    @Test
    public void testParseMap0304t() throws IOException {

        // Data
        Map<String, String> config = new HashMap<>();
        InputStream is = getClass().getResourceAsStream("/routes/map0304t");
        RouteParser parser = new SAMRouteParser(is, config);

        // Execute
        Route r = parser.parse();

        Assert.assertEquals("AAR-ZWD 4NM", r.getName());
        Assert.assertNull(r.getDeparture());
        Assert.assertNull(r.getDestination());

        Assert.assertEquals(47, r.getWaypoints().size());

        // Checking first waypoint
        Waypoint waypoint = r.getWaypoints().get(0);
        Position pos = Position.create(waypoint.getLatitude(), waypoint.getLongitude());
        Assert.assertEquals("001", waypoint.getName());
        Assert.assertEquals("56 09.206N", pos.getLatitudeAsString());
        Assert.assertEquals("010 13.254E", pos.getLongitudeAsString());
        Assert.assertEquals(0.300, waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(10.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdStarboard(), 0.0);

        // Checking second waypoint
        waypoint = r.getWaypoints().get(1);
        pos = Position.create(waypoint.getLatitude(), waypoint.getLongitude());
        Assert.assertEquals("002", waypoint.getName());
        Assert.assertEquals("56 09.881N", pos.getLatitudeAsString());
        Assert.assertEquals("010 13.953E", pos.getLongitudeAsString());

        Assert.assertEquals(0.300, waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(10.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdStarboard(), 0.0);

        // Checking third waypoint
        waypoint = r.getWaypoints().get(2);
        pos = Position.create(waypoint.getLatitude(), waypoint.getLongitude());
        Assert.assertEquals("003", waypoint.getName());
        Assert.assertEquals("56 09.127N", pos.getLatitudeAsString());
        Assert.assertEquals("010 16.057E", pos.getLongitudeAsString());

        Assert.assertEquals(3.00, waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(10.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdStarboard(), 0.0);

        // Checking last waypoint
        waypoint = r.getWaypoints().get(r.getWaypoints().size() - 1);
        pos = Position.create(waypoint.getLatitude(), waypoint.getLongitude());
        Assert.assertEquals("047", waypoint.getName());
        Assert.assertEquals("54 09.560N", pos.getLatitudeAsString());
        Assert.assertEquals("012 06.299E", pos.getLongitudeAsString());

        Assert.assertEquals(0.40, waypoint.getTurnRad(), 0.00001);
        Assert.assertEquals(10.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdStarboard(), 0.0);
    }

}
