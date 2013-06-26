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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;

import dk.dma.arcticweb.dao.ShipDaoImpl;
import dk.dma.embryo.domain.Permission;
import dk.dma.embryo.domain.Role;
import dk.dma.embryo.domain.Sailor;
import dk.dma.embryo.domain.SecuredUser;
import dk.dma.embryo.domain.Ship2;
import dk.dma.embryo.domain.Voyage;
import dk.dma.embryo.domain.VoyageInformation2;

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

        Ship2 ship = new Ship2(10L);
        entityManager.persist(ship);

        VoyageInformation2 voyageInformation = new VoyageInformation2(12, true);
        voyageInformation.addVoyageEntry(new Voyage("City1", "1.100", "2.000", LocalDateTime.parse("2013-06-19T12:23"),
                LocalDateTime.parse("2013-06-20T11:56")));
        voyageInformation.addVoyageEntry(new Voyage("City2", "3.300", "6.000", LocalDateTime.parse("2013-06-23T22:08"),
                LocalDateTime.parse("2013-06-25T20:19")));

        ship.setVoyageInformation(voyageInformation);
        entityManager.persist(voyageInformation);

        ///// new user
        sailor = new Sailor();
        sailor.add(perm1);
        entityManager.persist(sailor);

        user = new SecuredUser("user2", "pw2");
        user.addRole(sailor);
        entityManager.persist(user);

        ship = new Ship2(20L);
        entityManager.persist(ship);

        voyageInformation = new VoyageInformation2(20, true);

        ship.setVoyageInformation(voyageInformation);
        entityManager.persist(voyageInformation);
        
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
    public void getVoyageInformation_NoVoyagePlan() {
        VoyageInformation2 info = shipService.getVoyageInformation(20L);

        entityManager.clear();

        Assert.assertNotNull(info);
        Assert.assertEquals(Integer.valueOf(20), info.getPersonsOnboard());
        Assert.assertTrue(info.getDoctorOnboard().booleanValue());
        Assert.assertEquals(0, info.getVoyagePlan().size());
    }

    @Test
    public void getVoyageInformation_WithVoyagePlan() {
        VoyageInformation2 info = shipService.getVoyageInformation(10L);

        entityManager.clear();

        Assert.assertNotNull(info);
        Assert.assertEquals(Integer.valueOf(12), info.getPersonsOnboard());
        Assert.assertTrue(info.getDoctorOnboard().booleanValue());
        
        ReflectionAssert.assertPropertyLenientEquals("arrival", asList(LocalDateTime.parse("2013-06-19T12:23"), LocalDateTime.parse("2013-06-23T22:08")), info.getVoyagePlan());
    }
}
