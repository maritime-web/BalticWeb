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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import dk.dma.arcticweb.dao.GreenPosDao;
import dk.dma.arcticweb.dao.GreenPosDaoImpl;
import dk.dma.arcticweb.dao.ShipDao;
import dk.dma.arcticweb.dao.ShipDaoImpl;
import dk.dma.embryo.domain.GreenPosReport;
import dk.dma.embryo.domain.GreenPosSailingPlanReport;
import dk.dma.embryo.domain.Position;
import dk.dma.embryo.domain.Sailor;
import dk.dma.embryo.domain.SecuredUser;
import dk.dma.embryo.domain.Ship;
import dk.dma.embryo.domain.Voyage;
import dk.dma.embryo.domain.VoyagePlan;
import dk.dma.embryo.security.Subject;

/**
 * 
 * @author Jesper Tejlgaard
 */
public class GreenPosServiceImplTest {

    private static EntityManagerFactory factory;

    EntityManager entityManager;

    ShipService shipService;

    Subject subject;

    GreenPosService greenPosService;

    static Ship ship;

    ShipDao shipDao;

    GreenPosDao greenPosDao;

    @BeforeClass
    public static void setupForAll() {
        factory = Persistence.createEntityManagerFactory("componentTest");

        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();

        ship = new Ship();
        ship.setName("MyShip");
        ship.setCallsign("AA");
        ship.setMmsi(0L);

        VoyagePlan plan = new VoyagePlan();
        plan.addVoyageEntry(new Voyage("Nuuk", "64 10.4N", "051 43.5W", LocalDateTime.now(), LocalDateTime.now()
                .plusDays(2), 12, 0, true));
        ship.setVoyagePlan(plan);

        entityManager.persist(ship);

        entityManager.getTransaction().commit();
        entityManager.close();
    }

    @Before
    public void setup() {
        entityManager = factory.createEntityManager();
        shipDao = new ShipDaoImpl(entityManager);
        greenPosDao = new GreenPosDaoImpl(entityManager);
        
        subject = Mockito.mock(Subject.class);
        shipService = Mockito.mock(ShipService.class);
        
        greenPosService = new GreenPosServiceImpl(greenPosDao, shipDao, subject, shipService);
        
    }

    @After
    public void tearDown() {
        entityManager.close();
    }

    @Test
    public void testSave_GreenPosSailingPlanReport() {

        LocalDateTime datetime = LocalDateTime.now();

        GreenPosSailingPlanReport spReport = new GreenPosSailingPlanReport("MyShip", 0L, "AA", "M-ID", new Position(
                "64 10.400N", "051 43.500W"), "Weather", "Ice", 12.0, 343, "Nuuk", datetime, 12);

        entityManager.getTransaction().begin();

        Mockito.when(subject.hasRole(Sailor.class)).thenReturn(true);
        Mockito.when(subject.getUser()).thenReturn(new SecuredUser("Hans", "pwd"));
        Mockito.when(shipService.getYourShip()).thenReturn(ship);

        greenPosService.saveReport(spReport);

        entityManager.getTransaction().commit();

        entityManager.clear();

        entityManager.getTransaction().begin();

        LoggerFactory.getLogger(getClass()).error("WHYWHY");

        List<GreenPosReport> reports = greenPosService.listReports();
        Assert.assertEquals(1, reports.size());

        GreenPosSailingPlanReport spResult = (GreenPosSailingPlanReport) reports.get(0);

        Assert.assertEquals("MyShip", spResult.getShipName());
        Assert.assertEquals(Long.valueOf(0L), spResult.getShipMmsi());
        Assert.assertEquals("AA", spResult.getShipCallSign());
        Assert.assertEquals("M-ID", spResult.getShipMaritimeId());
        Assert.assertEquals("64 10.400N", spResult.getPosition().getLatitudeAsString());
        Assert.assertEquals("051 43.500W", spResult.getPosition().getLongitudeAsString());
        Assert.assertEquals("Weather", spResult.getWeather());
        Assert.assertEquals("Ice", spResult.getIceInformation());
        Assert.assertEquals(12.0, spResult.getSpeed(), 0.0);
        Assert.assertEquals(Integer.valueOf(343), spResult.getCourse());
        Assert.assertEquals("Nuuk", spResult.getDestination());
        Assert.assertEquals(datetime, spResult.getEtaOfArrival());
        Assert.assertEquals(Integer.valueOf(12), spResult.getPersonsOnBoard());

        entityManager.getTransaction().commit();
    }

}
