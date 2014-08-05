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
package dk.dma.embryo.vessel.model;

import org.junit.Assert;
import org.junit.Test;

import dk.dma.enav.model.voyage.Route;
import dk.dma.enav.model.voyage.RouteLeg;
import dk.dma.enav.model.voyage.RouteLeg.Heading;
import dk.dma.enav.model.voyage.Waypoint;

public class RouteTest {

    @Test
    public void testFromEnavModelRoute() {

        // DATA
        Route route = new Route("MyKey", "test route", "departure", "destination");

        Waypoint wp1 = new Waypoint("wp1", 55.5, 88.8, 0.5, 1.0);
        wp1.setRouteLeg(new RouteLeg(10.0, Heading.GC, 0.5, 0.7));
        route.getWaypoints().add(wp1);

        Waypoint wp2 = new Waypoint("wp2", 65.5, 44.4, 0.2, 0.5);
        wp2.setRouteLeg(new RouteLeg(20.0, Heading.RL, 0.4, 1.0));
        route.getWaypoints().add(wp2);

        // Execute
        dk.dma.embryo.vessel.model.Route transformed = dk.dma.embryo.vessel.model.Route.fromEnavModel(route);

        Assert.assertEquals("MyKey", transformed.getEnavId());
        Assert.assertEquals("test route", transformed.getName());
        Assert.assertEquals("departure", transformed.getOrigin());
        Assert.assertEquals("destination", transformed.getDestination());

        Assert.assertEquals(2, transformed.getWayPoints().size());

        WayPoint wpTrans = transformed.getWayPoints().get(0);
        Assert.assertEquals("wp1", wpTrans.getName());
        Assert.assertEquals(55.5, wpTrans.getPosition().getLatitude(), 0.0);
        Assert.assertEquals(88.8, wpTrans.getPosition().getLongitude(), 0.0);
        Assert.assertEquals(0.5, wpTrans.getRot(), 0.0);
        Assert.assertEquals(1.0, wpTrans.getTurnRadius(), 0.0);

        Assert.assertEquals(10.0, wpTrans.getLeg().getSpeed(), 0.0);
        Assert.assertEquals(0.5, wpTrans.getLeg().getXtdPort(), 0.0);
        Assert.assertEquals(0.7, wpTrans.getLeg().getXtdStarboard(), 0.0);

        wpTrans = transformed.getWayPoints().get(1);
        Assert.assertEquals("wp2", wpTrans.getName());
        Assert.assertEquals(65.5, wpTrans.getPosition().getLatitude(), 0.0);
        Assert.assertEquals(44.4, wpTrans.getPosition().getLongitude(), 0.0);
        Assert.assertEquals(0.2, wpTrans.getRot(), 0.0);
        Assert.assertEquals(0.5, wpTrans.getTurnRadius(), 0.0);

        Assert.assertEquals(20.0, wpTrans.getLeg().getSpeed(), 0.0);
        Assert.assertEquals(0.4, wpTrans.getLeg().getXtdPort(), 0.0);
        Assert.assertEquals(1.0, wpTrans.getLeg().getXtdStarboard(), 0.0);
    }

    @Test
    public void testFromEnavModelRoute_NoKey() {

        // DATA
        Route route = new Route(null, "test route", "departure", "destination");

        // Execute
        dk.dma.embryo.vessel.model.Route transformed = dk.dma.embryo.vessel.model.Route.fromEnavModel(route);

        Assert.assertNotNull(transformed.getEnavId());
        Assert.assertEquals("test route", transformed.getName());
        Assert.assertEquals("departure", transformed.getOrigin());
        Assert.assertEquals("destination", transformed.getDestination());
    }

}
