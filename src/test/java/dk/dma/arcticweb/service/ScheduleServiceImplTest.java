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
package dk.dma.arcticweb.service;

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

import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;
import org.unitils.reflectionassert.ReflectionComparatorMode;

import dk.dma.arcticweb.dao.ScheduleDaoImpl;
import dk.dma.embryo.domain.Permission;
import dk.dma.embryo.domain.Role;
import dk.dma.embryo.domain.Route;
import dk.dma.embryo.domain.RouteLeg;
import dk.dma.embryo.domain.Sailor;
import dk.dma.embryo.domain.SecuredUser;
import dk.dma.embryo.domain.Vessel;
import dk.dma.embryo.domain.Voyage;
import dk.dma.embryo.domain.WayPoint;
import dk.dma.embryo.validation.ConstraintViolationImpl;
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

        Permission perm1 = new Permission("yourShip");
        entityManager.persist(perm1);

        Role sailor = new Sailor();
        sailor.add(perm1);
        entityManager.persist(sailor);

        SecuredUser user = new SecuredUser("user1", "pw1");
        user.addRole(sailor);
        entityManager.persist(user);

        Vessel vessel = new Vessel(10L);
        entityManager.persist(vessel);

        vessel.addVoyageEntry(new Voyage("City1", "1 1.100N", "1 2.000W", LocalDateTime.parse("2013-06-19T12:23"),
                LocalDateTime.parse("2013-06-20T11:56"), 12, 0, true));
        vessel.addVoyageEntry(new Voyage("City2", "3 3.300N", "1 6.000W", LocalDateTime.parse("2013-06-23T22:08"),
                LocalDateTime.parse("2013-06-25T20:19"), 11, 0, false));

        for(Voyage v : vessel.getSchedule()){
            entityManager.persist(v);
        }
        
        // /// new user
        sailor = new Sailor();
        sailor.add(perm1);
        entityManager.persist(sailor);

        user = new SecuredUser("user2", "pw2");
        user.addRole(sailor);
        entityManager.persist(user);

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
                asList(LocalDateTime.parse("2013-06-19T12:23"), LocalDateTime.parse("2013-06-23T22:08")),
                voyages);
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
            expected.add(new ConstraintViolationImpl("wayPoints[0].turnRadius", null, null));
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
