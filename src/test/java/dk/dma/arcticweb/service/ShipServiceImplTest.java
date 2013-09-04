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
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;
import org.unitils.reflectionassert.ReflectionComparatorMode;

import dk.dma.arcticweb.dao.ShipDaoImpl;
import dk.dma.embryo.domain.Permission;
import dk.dma.embryo.domain.Role;
import dk.dma.embryo.domain.Route;
import dk.dma.embryo.domain.RouteLeg;
import dk.dma.embryo.domain.Sailor;
import dk.dma.embryo.domain.SecuredUser;
import dk.dma.embryo.domain.Ship;
import dk.dma.embryo.domain.Voyage;
import dk.dma.embryo.domain.VoyagePlan;
import dk.dma.embryo.domain.WayPoint;
import dk.dma.embryo.validation.ConstraintViolationImpl;

public class ShipServiceImplTest {

    private static EntityManagerFactory factory;
    private EntityManager entityManager;
    private ShipService shipService;

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

        Ship ship = new Ship(10L);
        entityManager.persist(ship);

        VoyagePlan voyagePlan = new VoyagePlan();
        voyagePlan.addVoyageEntry(new Voyage("City1", "1 1.100N", "1 2.000W", LocalDateTime.parse("2013-06-19T12:23"),
                LocalDateTime.parse("2013-06-20T11:56"), 12, true));
        voyagePlan.addVoyageEntry(new Voyage("City2", "3 3.300N", "1 6.000W", LocalDateTime.parse("2013-06-23T22:08"),
                LocalDateTime.parse("2013-06-25T20:19"), 11, false));

        ship.setVoyagePlan(voyagePlan);
        entityManager.persist(voyagePlan);

        // /// new user
        sailor = new Sailor();
        sailor.add(perm1);
        entityManager.persist(sailor);

        user = new SecuredUser("user2", "pw2");
        user.addRole(sailor);
        entityManager.persist(user);

        ship = new Ship(20L);
        entityManager.persist(ship);

        voyagePlan = new VoyagePlan();

        ship.setVoyagePlan(voyagePlan);
        entityManager.persist(voyagePlan);

        entityManager.getTransaction().commit();
        entityManager.close();
    }

    @Before
    public void setup() {
        entityManager = factory.createEntityManager();
        shipService = new ShipServiceImpl(new ShipDaoImpl(entityManager));
        entityManager.clear();
    }

    @Test
    public void getVoyagePlan_NoVoyagePlan() {
        VoyagePlan info = shipService.getVoyagePlan(20L);

        entityManager.clear();

        Assert.assertNotNull(info);
        Assert.assertEquals(0, info.getVoyagePlan().size());
    }

    @Test
    public void getVoyagePlan_WithVoyagePlan() {
        VoyagePlan info = shipService.getVoyagePlan(10L);

        entityManager.clear();

        Assert.assertNotNull(info);

        ReflectionAssert.assertPropertyLenientEquals("arrival",
                asList(LocalDateTime.parse("2013-06-19T12:23"), LocalDateTime.parse("2013-06-23T22:08")),
                info.getVoyagePlan());
    }

    @Test
    public void getVoyages_notExisting() {
        // TODO fix to work for several voyage plans
        List<Voyage> voyages = shipService.getVoyages(65L);

        Assert.assertNotNull(voyages);
        Assert.assertEquals(0, voyages.size());
    }

    @Test
    public void getVoyages_existing() {
        // TODO fix to work for several voyage plans
        List<Voyage> voyages = shipService.getVoyages(10L);

        Assert.assertNotNull(voyages);
        Assert.assertEquals(2, voyages.size());

        Assert.assertEquals("City1", voyages.get(0).getBerthName());
        Assert.assertEquals("City2", voyages.get(1).getBerthName());
    }

    @Test
    public void saveRoute_notExisting() {

        entityManager.getTransaction().begin();

        Route route = new Route("key", "name", "origin", "destination");

        WayPoint wp = new WayPoint("wp1", 61.0, 54.0, 0.5, 0.5);
        wp.setLeg(new RouteLeg(10.0, 1.0, 1.0));
        route.addWayPoint(wp);

        wp = new WayPoint("wp2", 61.0, 54.0, 1.0, 1.0);
        wp.setLeg(new RouteLeg(20.0, 2.0, 2.0));
        route.addWayPoint(wp);

        shipService.saveRoute(route);
        entityManager.getTransaction().commit();

        entityManager.clear();

        Route result = shipService.getRouteByEnavId("key");

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
        wp.setLeg(new RouteLeg(speed, xtdPort, xtdStarPort));
        route.addWayPoint(wp);

        try {
            shipService.saveRoute(route);
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
            expected.add(new ConstraintViolationImpl("wayPoints[0].leg.xtdPort", null, null));
            expected.add(new ConstraintViolationImpl("wayPoints[0].leg.xtdStarboard", null, null));

            Set<ConstraintViolation<?>> violations = ((ConstraintViolationException) e.getCause())
                    .getConstraintViolations();
            violations = ConstraintViolationImpl.fromOtherProvider(violations);

            ReflectionAssert.assertReflectionEquals(expected, violations, ReflectionComparatorMode.IGNORE_DEFAULTS,
                    ReflectionComparatorMode.LENIENT_ORDER);
            System.out.println(violations);
        }

        entityManager.clear();
    }

}
