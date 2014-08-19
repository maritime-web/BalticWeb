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
