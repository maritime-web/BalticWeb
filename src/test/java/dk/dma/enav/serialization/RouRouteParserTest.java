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
import org.junit.Test;

import dk.dma.enav.model.voyage.Route;
import dk.dma.enav.model.voyage.RouteLeg.Heading;
import dk.dma.enav.model.voyage.Waypoint;

/**
 * 
 * @author Jesper Tejlgaard
 */
public class RouRouteParserTest {

    @Test
    public void testParseValidStream() throws IOException {
        // Data
        Map<String, String> config = new HashMap<>();
        InputStream is = getClass().getResourceAsStream("/routes/Gdynia-Aarhus via The Sound.rou");
        RouteParser parser = new RouRouteParser(is, config);

        // Execute
        Route r = parser.parse();

        Assert.assertEquals("Gdynia-Aarhus via The Sound", r.getName());
        Assert.assertNull(r.getDeparture());
        Assert.assertNull(r.getDestination());

        Assert.assertEquals(26, r.getWaypoints().size());

        // Checking first waypoint
        Waypoint waypoint = r.getWaypoints().get(0);
        Assert.assertEquals("Gdynia", waypoint.getName());
        Assert.assertEquals(54.5303333, waypoint.getLatitude(), 0.0);
        Assert.assertEquals(18.6611167, waypoint.getLongitude(), 0.0);
        Assert.assertEquals(0.500, waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(10.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdStarboard(), 0.0);

        // Checking second waypoint
        waypoint = r.getWaypoints().get(1);
        Assert.assertEquals("WP_004", waypoint.getName());
        Assert.assertEquals(54.5273272, waypoint.getLatitude(), 0.0);
        Assert.assertEquals(18.8019717, waypoint.getLongitude(), 0.0);
        Assert.assertEquals(0.500, waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(10.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdStarboard(), 0.0);

        // Checking last waypoint
        waypoint = r.getWaypoints().get(r.getWaypoints().size() - 1);
        Assert.assertEquals("Aarhus", waypoint.getName());
        Assert.assertEquals(56.1499260, waypoint.getLatitude(), 0.0);
        Assert.assertEquals(10.2836996, waypoint.getLongitude(), 0.0);
        Assert.assertEquals(0.500, waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(10.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdStarboard(), 0.0);
    }

}
