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
package dk.dma.arcticweb.reporting.service;

import dk.dma.arcticweb.reporting.model.GreenPosReport;
import dk.dma.arcticweb.reporting.model.GreenPosSailingPlanReport;
import dk.dma.arcticweb.reporting.model.ReportedRoute;
import dk.dma.arcticweb.reporting.model.ReportedWayPoint;
import dk.dma.arcticweb.reporting.persistence.GreenPosDao;
import dk.dma.arcticweb.reporting.persistence.GreenPosDaoImpl;
import dk.dma.embryo.common.mail.MailSender;
import dk.dma.embryo.user.model.SailorRole;
import dk.dma.embryo.user.model.SecuredUser;
import dk.dma.embryo.user.persistence.RealmDao;
import dk.dma.embryo.user.security.Subject;
import dk.dma.embryo.vessel.model.Position;
import dk.dma.embryo.vessel.model.Route;
import dk.dma.embryo.vessel.model.Vessel;
import dk.dma.embryo.vessel.model.Voyage;
import dk.dma.embryo.vessel.model.WayPoint;
import dk.dma.embryo.vessel.persistence.ScheduleDao;
import dk.dma.embryo.vessel.persistence.VesselDao;
import dk.dma.embryo.vessel.persistence.VesselDaoImpl;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

/**
 * @author Jesper Tejlgaard
 */
public class GreenPosServiceImplTest {

    private static EntityManagerFactory factory;

    private static Vessel vessel;

    private EntityManager entityManager;

    private RealmDao realmDao;

    private Subject subject;

    private GreenPosService greenPosService;

    private VesselDao vesselDao;

    private GreenPosDao greenPosDao;

    private MailSender mailSender;

    private ScheduleDao scheduleDao;

    @BeforeClass
    public static void setupForAll() {
        factory = Persistence.createEntityManagerFactory("componentTest");

        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();

        vessel = new Vessel();
        vessel.getAisData().setName("MyShip");
        vessel.getAisData().setCallsign("AA");
        vessel.setMmsi(0L);

        entityManager.persist(vessel);

        Voyage v = new Voyage("Nuuk", "64 10.4N", "051 43.5W", DateTime.now(DateTimeZone.UTC), DateTime.now(
                DateTimeZone.UTC).plusDays(2), 12, 0, true);
        vessel.addVoyageEntry(v);
        entityManager.persist(v);

        entityManager.getTransaction().commit();
        entityManager.close();
    }

    @Before
    public void setup() {
        entityManager = factory.createEntityManager();
        vesselDao = new VesselDaoImpl(entityManager);
        greenPosDao = new GreenPosDaoImpl(entityManager);

        subject = Mockito.mock(Subject.class);
        realmDao = Mockito.mock(RealmDao.class);
        mailSender = Mockito.mock(MailSender.class);
        scheduleDao = Mockito.mock(ScheduleDao.class);

        greenPosService = new GreenPosServiceImpl(greenPosDao, vesselDao, subject, realmDao, mailSender, scheduleDao);

    }

    @After
    public void tearDown() {
        entityManager.close();
    }

    @Test
    public void testSave_GreenPosSailingPlanReport() {

        DateTime datetime = DateTime.now(DateTimeZone.UTC);

        GreenPosSailingPlanReport spReport = new GreenPosSailingPlanReport("MyShip", 0L, "AA", new Position(
                "64 10.400N", "051 43.500W"), 1, "Weather", "Ice", 12.0, 343, "Nuuk", datetime, 12,
                "Route with no particular good description", null);

        entityManager.getTransaction().begin();

        Mockito.when(subject.hasRole(SailorRole.class)).thenReturn(true);
        Mockito.when(subject.getUser()).thenReturn(new SecuredUser("Hans", "pwd", null));
        Mockito.when(subject.getUserId()).thenReturn(1L);
        SailorRole role = new SailorRole();
        role.setVessel(vessel);
        Mockito.when(realmDao.getSailor(1L)).thenReturn(role);

        Route route = new Route("myKey", "myName", "myOrigin", "myDestination");
        route.addWayPoint(new WayPoint("wp1", 60.0, -60.0, 1.0, 1.0));
        route.addWayPoint(new WayPoint("wp2", 62.0, -62.0, 1.0, 1.0));
        route.addWayPoint(new WayPoint("wp3", 64.0, -64.0, 1.0, 1.0));
        Mockito.when(scheduleDao.getActiveRoute(0L)).thenReturn(route);

        greenPosService.saveReport(spReport, null, null, Boolean.TRUE, "greenpos");

        entityManager.getTransaction().commit();

        entityManager.clear();

        entityManager.getTransaction().begin();

        List<GreenPosReport> reports = greenPosService.listReports();
        Assert.assertEquals(1, reports.size());

        GreenPosSailingPlanReport spResult = (GreenPosSailingPlanReport) reports.get(0);

        Assert.assertEquals("MyShip", spResult.getVesselName());
        Assert.assertEquals(Long.valueOf(0L), spResult.getVesselMmsi());
        Assert.assertEquals("AA", spResult.getVesselCallSign());
        Assert.assertEquals("64 10.400N", spResult.getPosition().getLatitudeAsString());
        Assert.assertEquals("051 43.500W", spResult.getPosition().getLongitudeAsString());
        Assert.assertEquals("Weather", spResult.getWeather());
        Assert.assertEquals("Ice", spResult.getIceInformation());
        Assert.assertEquals(12.0, spResult.getSpeed(), 0.0);
        Assert.assertEquals(Integer.valueOf(343), spResult.getCourse());
        Assert.assertEquals("Nuuk", spResult.getDestination());
        Assert.assertEquals(datetime.getMillis(), spResult.getEtaOfArrival().getMillis());
        Assert.assertEquals(Integer.valueOf(12), spResult.getPersonsOnBoard());
        Assert.assertEquals("Route with no particular good description", spResult.getRouteDescription());

        ReportedRoute reported = spReport.getRoute();
        assertEquals("myKey", reported.getEnavId());
        assertEquals("myName", reported.getName());

        List<ReportedWayPoint> expected = Arrays.asList(new ReportedWayPoint("wp1", 60.0, -60.0), new ReportedWayPoint(
                "wp2", 62.0, -62.0), new ReportedWayPoint("wp3", 64.0, -64.0));
        assertReflectionEquals(expected, reported.getWayPoints());

        entityManager.getTransaction().commit();
    }

}
