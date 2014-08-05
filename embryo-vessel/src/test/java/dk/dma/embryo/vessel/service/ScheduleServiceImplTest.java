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
package dk.dma.embryo.vessel.service;

import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.RollbackException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;
import org.unitils.reflectionassert.ReflectionComparatorMode;

import dk.dma.embryo.validation.ConstraintViolationImpl;
import dk.dma.embryo.vessel.model.Route;
import dk.dma.embryo.vessel.model.RouteLeg;
import dk.dma.embryo.vessel.model.Vessel;
import dk.dma.embryo.vessel.model.Voyage;
import dk.dma.embryo.vessel.model.WayPoint;
import dk.dma.embryo.vessel.persistence.ScheduleDaoImpl;
import dk.dma.enav.model.voyage.RouteLeg.Heading;

public class ScheduleServiceImplTest {

    private static EntityManagerFactory factory;
    private EntityManager entityManager;
    private ScheduleService vesselService;

    @BeforeClass
    public static void setupForAll() {
        factory = Persistence.createEntityManagerFactory("componentTest");

        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();

//        Role sailor = new SailorRole();
//        entityManager.persist(sailor);
//
//        SecuredUser user = new SecuredUser("user1", "pw1", null);
//        user.setRole(sailor);
//        entityManager.persist(user);

        Vessel vessel = new Vessel(10L);
        entityManager.persist(vessel);

        vessel.addVoyageEntry(new Voyage("City1", "1 1.100N", "1 2.000W", DateTime.parse("2013-06-19T12:23+00:00"),
                DateTime.parse("2013-06-20T11:56+00:00"), 12, 0, true));
        vessel.addVoyageEntry(new Voyage("City2", "3 3.300N", "1 6.000W", DateTime.parse("2013-06-23T22:08+00:00"),
                DateTime.parse("2013-06-25T20:19+00:00"), 11, 0, false));

        for (Voyage v : vessel.getSchedule()) {
            entityManager.persist(v);
        }

        // /// new user
//        sailor = new SailorRole();
//        entityManager.persist(sailor);
//
//        user = new SecuredUser("user2", "pw2", null);
//        user.setRole(sailor);
//        entityManager.persist(user);

        vessel = new Vessel(20L);
        entityManager.persist(vessel);

        entityManager.getTransaction().commit();
        entityManager.close();
    }

    @Before
    public void setup() {
        entityManager = factory.createEntityManager();
        vesselService = new ScheduleServiceImpl(new ScheduleDaoImpl(entityManager));
        entityManager.clear();
    }

    @Test
    @Ignore
    public void getVoyagePlan_NoVoyagePlan() {
        List<Voyage> info = vesselService.getSchedule(20L);

        entityManager.clear();

        Assert.assertNotNull(info);
        Assert.assertEquals(0, info.size());
    }

    @Test
    @Ignore
    public void getVoyagePlan_WithVoyagePlan() {
        List<Voyage> voyages = vesselService.getSchedule(10L);

        entityManager.clear();

        Assert.assertNotNull(voyages);

        ReflectionAssert.assertPropertyLenientEquals("arrival",
                asList(DateTime.parse("2013-06-19T12:23+00:00"), DateTime.parse("2013-06-23T22:08+00:00")), voyages);
    }

    @Test
    public void saveRoute_notExisting() {

        entityManager.getTransaction().begin();

        Route route = new Route("key", "name", "origin", "destination");

        WayPoint wp = new WayPoint("wp1", 61.0, 54.0, 0.5, 0.5);
        wp.setLeg(new RouteLeg(10.0, 1.0, 1.0, Heading.RL));
        route.addWayPoint(wp);

        wp = new WayPoint("wp2", 61.0, 54.0, 1.0, 1.0);
        wp.setLeg(new RouteLeg(20.0, 2.0, 2.0, Heading.RL));
        route.addWayPoint(wp);

        vesselService.saveRoute(route);
        entityManager.getTransaction().commit();

        entityManager.clear();

        Route result = vesselService.getRouteByEnavId("key");

        Assert.assertNotNull(result);
        Assert.assertEquals("key", result.getEnavId());
        Assert.assertEquals("name", result.getName());
        Assert.assertEquals("origin", result.getOrigin());
        Assert.assertEquals("destination", result.getDestination());

        Assert.assertNotNull(result.getWayPoints());
        Assert.assertEquals(2, result.getWayPoints().size());
        Assert.assertEquals("wp1", result.getWayPoints().get(0).getName());
        Assert.assertEquals(61.0, result.getWayPoints().get(0).getPosition().getLatitude(), 0.0);
        Assert.assertEquals(54.0, result.getWayPoints().get(0).getPosition().getLongitude(), 0.0);
        Assert.assertEquals(.5, result.getWayPoints().get(0).getRot(), 0.0);
        Assert.assertEquals(.5, result.getWayPoints().get(0).getTurnRadius(), 0.0);

        Assert.assertEquals("wp2", result.getWayPoints().get(1).getName());
        Assert.assertEquals(61.0, result.getWayPoints().get(1).getPosition().getLatitude(), 0.0);
        Assert.assertEquals(54.0, result.getWayPoints().get(1).getPosition().getLongitude(), 0.0);
        Assert.assertEquals(1.0, result.getWayPoints().get(1).getRot(), 0.0);
        Assert.assertEquals(1.0, result.getWayPoints().get(1).getTurnRadius(), 0.0);

    }

    @Test
    public void saveRoute_InvalidData_EmptyValues() {
        entityManager.getTransaction().begin();

        String noKey = null;
        String name = null;
        String origin = null;
        String destination = null;

        Route route = new Route(noKey, name, origin, destination);

        String wp_name = null;
        Double longitude = null;
        Double latitude = null;
        Double turnRadius = null;
        Double rotation = null;
        Double speed = null;
        Double xtdPort = null;
        Double xtdStarPort = null;

        WayPoint wp = new WayPoint(wp_name, latitude, longitude, rotation, turnRadius);
        wp.setLeg(new RouteLeg(speed, xtdPort, xtdStarPort, Heading.RL));
        route.addWayPoint(wp);

        try {
            vesselService.saveRoute(route);
            entityManager.getTransaction().commit();

            Assert.fail("Constraint violations expected");
        } catch (RollbackException e) {
            Assert.assertEquals(ConstraintViolationException.class, e.getCause().getClass());

            // Expectation
            Set<ConstraintViolation<?>> expected = new HashSet<>();
            expected.add(new ConstraintViolationImpl("name", null, null));
            expected.add(new ConstraintViolationImpl("wayPoints[0].name", null, null));
            expected.add(new ConstraintViolationImpl("wayPoints[0].position.latitude", null, null));
            expected.add(new ConstraintViolationImpl("wayPoints[0].position.longitude", null, null));
            expected.add(new ConstraintViolationImpl("wayPoints[0].leg.speed", null, null));

            Set<ConstraintViolation<?>> violations = ((ConstraintViolationException) e.getCause())
                    .getConstraintViolations();
            violations = ConstraintViolationImpl.fromOtherProvider(violations);

            ReflectionAssert.assertReflectionEquals(expected, violations, ReflectionComparatorMode.IGNORE_DEFAULTS,
                    ReflectionComparatorMode.LENIENT_ORDER);
        }

        entityManager.clear();
    }

}
