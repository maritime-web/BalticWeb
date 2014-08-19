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
