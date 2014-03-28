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
package dk.dma.embryo.vessel.component;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import dk.dma.embryo.vessel.component.RouteDecorator;
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
