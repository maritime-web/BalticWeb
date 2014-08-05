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
package dk.dma.arcticweb.reporting.model;

import static org.junit.Assert.assertEquals;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import dk.dma.embryo.vessel.model.Route;
import dk.dma.embryo.vessel.model.WayPoint;

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
