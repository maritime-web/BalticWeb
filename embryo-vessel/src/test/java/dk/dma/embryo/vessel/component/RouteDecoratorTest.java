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
package dk.dma.embryo.vessel.component;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import dk.dma.enav.model.voyage.Route;
import dk.dma.enav.model.voyage.RouteLeg;
import dk.dma.enav.model.voyage.RouteLeg.Heading;
import dk.dma.enav.model.voyage.Waypoint;

/**
 * @author Jesper Tejlgaard
 */
public class RouteDecoratorTest {

    private Route route;
    
    @Before
    public void setup(){
        route = new Route();
        Waypoint wp = new Waypoint("wp1", 10.0, 10.0, 0.5, 0.5);
        wp.setRouteLeg(new RouteLeg(10.0, Heading.RL, 2.0, 2.0));
        route.getWaypoints().add(wp);

        wp = new Waypoint("wp2", 20.0, 20.0, 0.0, 0.5);
        wp.setRouteLeg(new RouteLeg(20.0, Heading.RL, 2.0, 2.0));
        route.getWaypoints().add(wp);

        wp = new Waypoint("wp3", 30.0, 30.0, 0.0, 0.5);
        wp.setRouteLeg(new RouteLeg(30.0, Heading.RL, 2.0, 2.0));
        route.getWaypoints().add(wp);
}
    
    @Test
    public void testConstruction_NoWaypointEtas() {
        RouteDecorator dec = new RouteDecorator(route);
    }
    @Test
    public void testConstruction_FirstWaypointEtas() {
        route.getWaypoints().get(0).setEta(new Date());
        RouteDecorator dec = new RouteDecorator(route);
    }

}
