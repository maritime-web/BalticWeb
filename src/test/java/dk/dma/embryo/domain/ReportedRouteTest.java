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
package dk.dma.embryo.domain;

import static org.junit.Assert.assertEquals;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * @author Jesper Tejlgaard
 */
public class ReportedRouteTest {

    @Test
    public void testFrom() {
        // TEST DATA
        Route route = new Route("myKey", "myName", "myOrigin", "myDestination");
        route.addWayPoint(new WayPoint("wp1", 60.0, -60.0, 1.0, 1.0));
        route.addWayPoint(new WayPoint("wp2", 62.0, -62.0, 1.0, 1.0));
        route.addWayPoint(new WayPoint("wp3", 64.0, -64.0, 1.0, 1.0));

        // EXECUTE
        ReportedRoute reported = ReportedRoute.fromModel(route);

        // VERIFY
        assertEquals("myKey", reported.getEnavId());
        assertEquals("myName", reported.getName());

        List<ReportedWayPoint> expected = Arrays.asList(new ReportedWayPoint("wp1", 60.0, -60.0), new ReportedWayPoint(
                "wp2", 62.0, -62.0), new ReportedWayPoint("wp3", 64.0, -64.0));
        assertReflectionEquals(expected, reported.getWayPoints());
    }

    @Test
    public void testGetWayPointsAsString() {
        // TEST DATA
        ReportedRoute reportedRoute = new ReportedRoute("myKey", "myName");
        reportedRoute.addWayPoint(new ReportedWayPoint("wp1", "60 00.000N", "060 00.000W"));
        reportedRoute.addWayPoint(new ReportedWayPoint("wp2", 62.0, -62.0));
        reportedRoute.addWayPoint(new ReportedWayPoint("wp3", 64.0, -64.0));

        // EXECUTE
        String result = reportedRoute.getWayPointsAsString();

        // VERIFY
        assertEquals("[60 00.000N,060 00.000W],  [62 00.000N,062 00.000W],  [64 00.000N,064 00.000W]", result);
    }
}
