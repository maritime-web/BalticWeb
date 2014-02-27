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

import dk.dma.enav.model.geometry.Position;
import dk.dma.enav.model.voyage.Route;
import dk.dma.enav.model.voyage.RouteLeg.Heading;
import dk.dma.enav.model.voyage.Waypoint;

/**
 * 
 * @author Jesper Tejlgaard
 */
public class SimpleRouteParserTest {

    @Test
    public void testParseValidStream() throws IOException {
        // Data
        Map<String, String> config = new HashMap<>();
        InputStream is = getClass().getResourceAsStream("/routes/CPH-AAH.txt");
        SimpleRouteParser parser = new SimpleRouteParser(is, config);

        // Execute
        Route r = parser.parse();
        
        Assert.assertEquals("Example route", r.getName());
        Assert.assertEquals("Copenhagen", r.getDeparture());
        Assert.assertEquals("Aarhus", r.getDestination());
        Assert.assertEquals(13, r.getWaypoints().size());

        Assert.assertEquals(13, r.getWaypoints().size());
        
        //Checking first waypoint
        Waypoint waypoint = r.getWaypoints().get(0);
        Assert.assertEquals("WP_001", waypoint.getName());
        Assert.assertEquals("55 42.510N", Position.create(waypoint.getLatitude(), 0).getLatitudeAsString());
        Assert.assertEquals("012 36.724E", Position.create(0, waypoint.getLongitude()).getLongitudeAsString());
        Assert.assertEquals(0.500, waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(10.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdStarboard(), 0.0);

        //Checking second waypoint
        waypoint = r.getWaypoints().get(1);
        Assert.assertEquals("WP_002", waypoint.getName());
        Assert.assertEquals("55 45.920N", Position.create(waypoint.getLatitude(), 0).getLatitudeAsString());
        Assert.assertEquals("012 40.554E", Position.create(0, waypoint.getLongitude()).getLongitudeAsString());
        Assert.assertEquals(0.500, waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(10.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdStarboard(), 0.0);

        //Checking last waypoint
        waypoint = r.getWaypoints().get(r.getWaypoints().size()-1);
        Assert.assertEquals("WP_013", waypoint.getName());
        Assert.assertEquals("56 09.755N", Position.create(waypoint.getLatitude(), 0).getLatitudeAsString());
        Assert.assertEquals("010 14.404E", Position.create(0, waypoint.getLongitude()).getLongitudeAsString());
        Assert.assertEquals(0.500, waypoint.getTurnRad(), 0.0);
        Assert.assertEquals(10.00, waypoint.getRouteLeg().getSpeed(), 0.0);
        Assert.assertEquals(Heading.RL, waypoint.getRouteLeg().getHeading());
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.100, waypoint.getRouteLeg().getXtdStarboard(), 0.0);
    }
    
}
