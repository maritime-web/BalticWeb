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

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;

import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.slf4j.Logger;

import dk.dma.arcticweb.dao.RealmDao;
import dk.dma.arcticweb.dao.ShipDao;
import dk.dma.embryo.domain.AuthorityRole;
import dk.dma.embryo.domain.Berth;
import dk.dma.embryo.domain.GreenPosFinalReport;
import dk.dma.embryo.domain.GreenPosPositionReport;
import dk.dma.embryo.domain.GreenPosReport;
import dk.dma.embryo.domain.GreenPosSailingPlanReport;
import dk.dma.embryo.domain.IEntity;
import dk.dma.embryo.domain.Permission;
import dk.dma.embryo.domain.Position;
import dk.dma.embryo.domain.ReportedVoyage;
import dk.dma.embryo.domain.Role;
import dk.dma.embryo.domain.Route;
import dk.dma.embryo.domain.Sailor;
import dk.dma.embryo.domain.SecuredUser;
import dk.dma.embryo.domain.Ship;
import dk.dma.embryo.domain.ShoreRole;
import dk.dma.embryo.domain.Voyage;
import dk.dma.embryo.domain.VoyagePlan;
import dk.dma.embryo.rest.util.DateTimeConverter;

@Singleton
@Startup
public class TestServiceBean {

    @Inject
    RealmDao realmDao;

    @EJB
    private ShipDao shipDao;

    @EJB
    private ShipService shipService;

    @Inject
    private Logger logger;

    @Inject
    private EntityManagerFactory emf;

    @PostConstruct
    public void startup() {
        Map<String, Object> props = emf.getProperties();

        String hbm2dllAuto = (String) props.get("hibernate.hbm2ddl.auto");
        logger.info("Detected database auto update setting: {}", hbm2dllAuto);

        if ("create-drop".equals(hbm2dllAuto)) {
            createTestData();
        }
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void clearAllData() {
        logger.info("Deleting existing entries");

        deleteAll(Berth.class);
        deleteAll(Ship.class);
        deleteAll(VoyagePlan.class);
        // delete any other voyages
        deleteAll(Voyage.class);
        deleteAll(Route.class);
        deleteAll(SecuredUser.class);
        deleteAll(Role.class);
        deleteAll(Permission.class);
        deleteAll(GreenPosReport.class);

        logger.info("AFTER DELETION");
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public <E extends IEntity<K>, K> void deleteAll(Class<E> type) {
        try {
            logger.info("Deleting entities of type {}", type.getName());

            List<E> entities = shipDao.getAll(type);
            for (E entity : entities) {
                logger.info("Deleting entities with id {}", entity.getId());
                shipDao.remove(entity);
            }
        } catch (RuntimeException e) {
            logger.error("Error deleting existing entries", e);
            throw e;
        }
    }

    public void createTestData() {
        createOrasilaTestData();
        uploadOrasilaRoutes();
        createOraTankTestData();
        uploadOraTankRoutes();
        createSarfaqTestData();
        uploadSarfaqRoutes();
        createCarnivalLegendTestData();
        uploadCarnivalLegendRoutes();

        createDmiLogin();
        createIceCenterLogin();
        createArcticCommandLogin();
        createGreenposReports();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    private void createOrasilaTestData() {
        logger.info("BEFORE CREATION - ORASILA");

        // Create ship and user
        Ship newShip = new Ship();
        newShip.setName("ORASILA");
        newShip.setMmsi(220443000L);
        newShip.setCallsign("OYDK2");
        newShip.setImoNo(9336725L);
        newShip.setType("TANKER");
        newShip.setCommCapabilities("Sat C 0581 422044310, GMDSS A1+A2+A3");
        newShip.setWidth(14);
        newShip.setLength(77);
        newShip.setMaxSpeed(BigDecimal.valueOf(12.6));
        newShip.setGrossTonnage(2194);
        newShip.setIceClass("A1");
        newShip.setHelipad(Boolean.FALSE);

        newShip = shipDao.saveEntity(newShip);

        Permission ais = new Permission("ais");
        Permission yourShip = new Permission("yourShip");

        shipDao.saveEntity(ais);
        shipDao.saveEntity(yourShip);

        Sailor sailorRole = new Sailor();
        sailorRole.setShip(newShip);
        sailorRole.add(ais);
        sailorRole.add(yourShip);

        shipDao.saveEntity(sailorRole);

        SecuredUser user = new SecuredUser("orasila", "qwerty", "obo@dma.dk");
        user.addRole(sailorRole);

        shipDao.saveEntity(user);

        // Create auth and user
        AuthorityRole auth = new AuthorityRole();
        // auth.setName(new Text("en", "Danish Maritime Authority"));
        auth.add(ais);
        shipDao.saveEntity(auth);

        user = new SecuredUser("dma", "qwerty", "obo@dma.dk");
        user.addRole(auth);

        shipDao.saveEntity(user);

        // Test data found on
        // http://gronlandskehavnelods.dk/#HID=78
        shipDao.saveEntity(new Berth("Nuuk", "64 10.4N", "051 43.5W"));
        shipDao.saveEntity(new Berth("Sisimiut", "Holsteinsborg", "66 56.5N", "053 40.5W"));
        shipDao.saveEntity(new Berth("Danmarkshavn", "76 46.0N", "018 45.0W"));
        shipDao.saveEntity(new Berth("Kangilinnguit", "Grønnedal", "61 14.3N", "48 06.1W"));
        shipDao.saveEntity(new Berth("Aasiaat", "Egedesminde", "68 42.6N", "052 53.0W"));
        shipDao.saveEntity(new Berth("Ilulissat", "Jakobshavn", "69 13.5N", "051 06.0W"));
        shipDao.saveEntity(new Berth("Qeqertarsuaq", "Godhavn", "69 15.0N", "053 33.0W"));
        shipDao.saveEntity(new Berth("Ammassivik", "Sletten", "60 35.8N", "045 23.7W"));
        shipDao.saveEntity(new Berth("Ittaajimmiut", "Kap Hope", "70 27.5N", "022 22.0W"));
        shipDao.saveEntity(new Berth("Kangersuatsiaq", "Prøven", "72 22.7N", "055 33.5W"));
        shipDao.saveEntity(new Berth("Qaanaaq", "Thule", "77 27.8N", "069 14.0W"));
        shipDao.saveEntity(new Berth("Upernavik", "72 47.5N", "056 09.4W"));
        shipDao.saveEntity(new Berth("Miami", "25 47.16N", "08 13.27W"));

        LocalDateTime now = LocalDateTime.now();
        VoyagePlan voyagePlan = new VoyagePlan();
        newShip.setVoyagePlan(voyagePlan);

        voyagePlan.addVoyageEntry(new Voyage("Miami", "25 47.16N", "08 13.27W", now.minusDays(4).withTime(9, 30, 0, 0),
                now.minusDays(3).withTime(17, 0, 0, 0), 12, true));
        voyagePlan.addVoyageEntry(new Voyage("Nuuk", "64 10.4N", "051 43.5W", now.plusDays(3).withTime(10, 30, 0, 0),
                now.plusDays(5).withTime(9, 0, 0, 0), 12, true));
        voyagePlan.addVoyageEntry(new Voyage("Thule", "77 27.8N", "069 14.0W", now.plusDays(9).withTime(13, 15, 0, 0),
                now.plusDays(11).withTime(9, 0, 0, 0)));
        voyagePlan.addVoyageEntry(new Voyage("Upernavik", "72 47.5N", "056 09.4W", now.plusDays(13).withTime(10, 45, 0,
                0), now.plusDays(14).withTime(9, 30, 0, 0)));

        shipDao.saveEntity(voyagePlan);

    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    private void uploadOrasilaRoutes() {
        logger.info("BEFORE UPLOAD - ORASILA");

        VoyagePlan voyagePlan = shipService.getVoyagePlan(220443000L);
        insertDemoRoute(voyagePlan.getVoyagePlan().get(0).getEnavId(), "/demo/routes/Miami-Nuuk.txt", true);
        insertDemoRoute(voyagePlan.getVoyagePlan().get(1).getEnavId(), "/demo/routes/Nuuk-Thule.txt", false);
        insertDemoRoute(voyagePlan.getVoyagePlan().get(2).getEnavId(), "/demo/routes/Thule-Upernavik.txt", false);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    private void createOraTankTestData() {
        logger.info("BEFORE CREATION - ORATANK");

        // Create ship and user
        Ship newShip = new Ship();
        newShip.setName("ORATANK");
        newShip.setMmsi(220516000L);
        newShip.setCallsign("OXPJ2");
        newShip = shipDao.saveEntity(newShip);

        Permission ais = new Permission("ais");
        Permission yourShip = new Permission("yourShip");

        shipDao.saveEntity(ais);
        shipDao.saveEntity(yourShip);

        Sailor sailorRole = new Sailor();
        sailorRole.setShip(newShip);
        sailorRole.add(ais);
        sailorRole.add(yourShip);

        shipDao.saveEntity(sailorRole);

        SecuredUser user = new SecuredUser("oratank", "qwerty", "obo@dma.dk");
        user.addRole(sailorRole);

        shipDao.saveEntity(user);

        LocalDateTime now = LocalDateTime.now();
        VoyagePlan voyagePlan = new VoyagePlan();
        newShip.setVoyagePlan(voyagePlan);

        voyagePlan.addVoyageEntry(new Voyage("Nuuk", "64 10.4N", "051 43.5W", now.plusDays(3).withTime(10, 30, 0, 0),
                now.plusDays(5).withTime(9, 0, 0, 0)));
        voyagePlan.addVoyageEntry(new Voyage("X", "63 41.81N", "051 29.00W", now.minusDays(4).withTime(9, 30, 0, 0),
                now.minusDays(3).withTime(17, 0, 0, 0)));

        shipDao.saveEntity(voyagePlan);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    private void uploadOraTankRoutes() {
        logger.info("BEFORE UPLOAD - ORATANK");

        VoyagePlan voyagePlan = shipService.getVoyagePlan(220516000L);
        insertDemoRoute(voyagePlan.getVoyagePlan().get(0).getEnavId(), "/demo/routes/Oratank-Nuuk.txt", true);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    private void createSarfaqTestData() {
        logger.info("BEFORE CREATION - SARFAQ ITTUK");

        // Create ship and user
        Ship newShip = new Ship();
        newShip.setName("SARFAQ ITTUK");
        newShip.setMmsi(331037000L);
        newShip.setCallsign("OWDD");
        newShip = shipDao.saveEntity(newShip);

        Permission ais = new Permission("ais");
        Permission yourShip = new Permission("yourShip");

        shipDao.saveEntity(ais);
        shipDao.saveEntity(yourShip);

        Sailor sailorRole = new Sailor();
        sailorRole.setShip(newShip);
        sailorRole.add(ais);
        sailorRole.add(yourShip);

        shipDao.saveEntity(sailorRole);

        SecuredUser user = new SecuredUser("sarfaq", "qwerty", "obo@dma.dk");
        user.addRole(sailorRole);

        shipDao.saveEntity(user);

        LocalDateTime now = LocalDateTime.now();
        VoyagePlan voyagePlan = new VoyagePlan();
        newShip.setVoyagePlan(voyagePlan);

        DateTimeConverter converter = DateTimeConverter.getDateTimeConverter();

        LocalDateTime firstDeparture = converter.toObject("27-09-2013 21:00", null);

        voyagePlan.addVoyageEntry(new Voyage("Nuuk", "64 10.4N", "051 43.5W", null, firstDeparture));
        voyagePlan.addVoyageEntry(new Voyage("Maniitsoq", "65 24.8N", "052 54.3W", converter.toObject(
                "28-09-2013 07:00", null), converter.toObject("28-09-2013 07:30", null)));
        voyagePlan.addVoyageEntry(new Voyage("Kangaamiut", "65 49.6N", "053 20.9W", converter.toObject(
                "28-09-2013 10:45", null), converter.toObject("28-09-2013 11:00", null)));
        voyagePlan.addVoyageEntry(new Voyage("Sisimiut", "66 56.5N", "053 40.5W", converter.toObject(
                "28-09-2013 18:00", null), converter.toObject("28-09-2013 21:00", null)));
        voyagePlan.addVoyageEntry(new Voyage("Aasiaat", "68 42.6N", "052 53.0W", converter.toObject("29-09-2013 08:00",
                null), converter.toObject("29-09-2013 08:30", null)));
        voyagePlan.addVoyageEntry(new Voyage("Ilulissat", "69 13.5N", "051 06.0W", converter.toObject(
                "29-09-2013 13:00", null), converter.toObject("29-09-2013 17:00", null)));
        voyagePlan.addVoyageEntry(new Voyage("Aasiaat", "68 42.6N", "052 53.0W", converter.toObject("29-09-2013 21:30",
                null), converter.toObject("29-09-2013 22:00", null)));
        voyagePlan.addVoyageEntry(new Voyage("Sisimiut", "66 56.5N", "053 40.5W", converter.toObject(
                "30-09-2013 09:00", null), converter.toObject("30-09-2013 10:30", null)));
        voyagePlan.addVoyageEntry(new Voyage("Kangaamiut", "65 49.6N", "053 20.9W", converter.toObject(
                "30-09-2013 17:30", null), converter.toObject("30-09-2013 17:45", null)));
        voyagePlan.addVoyageEntry(new Voyage("Maniitsoq", "65 24.8N", "052 54.3W", converter.toObject(
                "30-09-2013 21:30", null), converter.toObject("30-09-2013 22:00", null)));
        voyagePlan.addVoyageEntry(new Voyage("Nuuk", "64 10.4N", "051 43.5W", converter.toObject("01-10-2013 06:30",
                null), converter.toObject("01-10-2013 09:00", null)));
        voyagePlan.addVoyageEntry(new Voyage("Qeqertarsuatsiaat", "63 05.4N", "050 41.0W", converter.toObject(
                "01-10-2013 16:30", null), converter.toObject("01-10-2013 16:45", null)));

        voyagePlan.addVoyageEntry(new Voyage("Paamiut", "61 59.8N", "049 40.8W", converter.toObject("01-10-2013 23:30",
                null), converter.toObject("02-10-2013 00:00", null)));
        voyagePlan.addVoyageEntry(new Voyage("Arsuk", "61 10.5N", "048 27.1W", converter.toObject("02-10-2013 06:45",
                null), converter.toObject("02-10-2013 07:00", null)));
        voyagePlan.addVoyageEntry(new Voyage("Qaqortoq", "60 43.1N", "046 02.4W", converter.toObject(
                "02-10-2013 15:30", null), converter.toObject("02-10-2013 19:00", null)));
        voyagePlan.addVoyageEntry(new Voyage("Narsaq", "60 54.5N", "046 03.0W", converter.toObject("02-10-2013 21:00",
                null), converter.toObject("02-10-2013 21:30", null)));

        voyagePlan.addVoyageEntry(new Voyage("Arsuk", "61 10.5N", "048 27.2W", converter.toObject("03-10-2013 06:45",
                null), converter.toObject("03-10-2013 07:00", null)));
        voyagePlan.addVoyageEntry(new Voyage("Paamiut", "61 59.8N", "049 40.8W", converter.toObject("03-10-2013 13:30",
                null), converter.toObject("03-10-2013 14:30", null)));
        voyagePlan.addVoyageEntry(new Voyage("Qeqertarsuatsiaat", "63 05.4N", "050 41.0W", converter.toObject(
                "03-10-2013 22:30", null), converter.toObject("03-10-2013 22:45", null)));
        voyagePlan.addVoyageEntry(new Voyage("Nuuk", "64 10.4N", "051 43.5W", converter.toObject("04-10-2013 09:00",
                null), null));

        // firstDeparture.

        Period p = new Period(firstDeparture, now);
        if (p.getWeeks() > 0) {
            for (Voyage v : voyagePlan.getVoyagePlan()) {
                v.setArrival(v.getArrival().plusWeeks(p.getWeeks()));
                v.setDeparture(v.getDeparture().plusWeeks(p.getWeeks()));
            }
        }

        shipDao.saveEntity(voyagePlan);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    private void uploadSarfaqRoutes() {
        logger.info("BEFORE UPLOAD - SARFAQ");

        VoyagePlan voyagePlan = shipService.getVoyagePlan(331037000L);
        insertDemoRoute(voyagePlan.getVoyagePlan().get(0).getEnavId(), "/demo/routes/SARFAQ-Nuuk-Maniitsoq.txt", true);
        insertDemoRoute(voyagePlan.getVoyagePlan().get(1).getEnavId(), "/demo/routes/SARFAQ-Maniitsoq-Kangaamiut.txt",
                false);
        insertDemoRoute(voyagePlan.getVoyagePlan().get(2).getEnavId(), "/demo/routes/SARFAQ-Kangaamiut-Sisimiut.txt",
                false);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    private void createCarnivalLegendTestData() {
        logger.info("BEFORE CREATION - CARNIVAL LEGEND");

        // Create ship and user
        Ship newShip = new Ship();
        newShip.setName("CARNIVAL LEGEND");
        newShip.setMmsi(354237000L);
        newShip.setCallsign("H3VT");
        newShip.setImoNo(9224726L);
        newShip.setLength(293);
        newShip.setWidth(32);
        newShip.setGrossTonnage(85942);
        newShip = shipDao.saveEntity(newShip);

        Permission ais = new Permission("ais");
        Permission yourShip = new Permission("yourShip");

        shipDao.saveEntity(ais);
        shipDao.saveEntity(yourShip);

        Sailor sailorRole = new Sailor();
        sailorRole.setShip(newShip);
        sailorRole.add(ais);
        sailorRole.add(yourShip);

        shipDao.saveEntity(sailorRole);

        SecuredUser user = new SecuredUser("carnivalLegend", "qwerty", "obo@dma.dk");
        user.addRole(sailorRole);

        shipDao.saveEntity(user);

        LocalDateTime now = LocalDateTime.now();
        VoyagePlan voyagePlan = new VoyagePlan();
        newShip.setVoyagePlan(voyagePlan);

        voyagePlan.addVoyageEntry(new Voyage("Copenhagen", "55 67.61N", "12 56.83E", null, now.withTime(12, 57, 0, 0),
                300, true));
        voyagePlan.addVoyageEntry(new Voyage("Nuuk", "64 10.4N", "051 43.5W", now.plusDays(10).withTime(7, 10, 0, 0),
                null));

        // firstDeparture.

        shipDao.saveEntity(voyagePlan);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    private void uploadCarnivalLegendRoutes() {
        logger.info("BEFORE UPLOAD - CARNIVAL LEGEND");

        VoyagePlan voyagePlan = shipService.getVoyagePlan(354237000L);
        insertDemoRoute(voyagePlan.getVoyagePlan().get(0).getEnavId(), "/demo/routes/CARNIVAL-LEGEND-Cph-Nuuk.txt", true);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    private void createArcticCommandLogin() {
        logger.info("BEFORE CREATION - Arctic Command");

        // Create ship and user
        Permission ais = new Permission("ais");
        Permission ice = new Permission("ice");
        Permission msi = new Permission("msi");
        Permission assistance = new Permission("assistance");
        Permission greenpos = new Permission("greenpos");

        shipDao.saveEntity(ais);
        shipDao.saveEntity(ice);
        shipDao.saveEntity(msi);
        shipDao.saveEntity(assistance);
        shipDao.saveEntity(greenpos);

        ShoreRole role = new ShoreRole();
        role.add(ais);
        role.add(ice);
        role.add(msi);
        role.add(assistance);
        role.add(greenpos);

        shipDao.saveEntity(role);

        SecuredUser user = new SecuredUser("arcticCommand", "qwerty", "obo@dma.dk");
        user.addRole(role);

        shipDao.saveEntity(user);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    private void createDmiLogin() {
        logger.info("BEFORE CREATION - DMI");

        // Create ship and user
        Permission ais = new Permission("ais");
        Permission ice = new Permission("ice");
        Permission msi = new Permission("msi");
        Permission assistance = new Permission("assistance");

        shipDao.saveEntity(ais);
        shipDao.saveEntity(ice);
        shipDao.saveEntity(msi);
        shipDao.saveEntity(assistance);

        ShoreRole role = new ShoreRole();
        role.add(ais);
        role.add(ice);
        role.add(msi);
        role.add(assistance);

        shipDao.saveEntity(role);

        SecuredUser user = new SecuredUser("dmi", "qwerty", "obo@dma.dk");
        user.addRole(role);

        shipDao.saveEntity(user);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    private void createIceCenterLogin() {
        logger.info("BEFORE CREATION - Istjejenesten");

        // Create ship and user
        Permission ais = new Permission("ais");
        Permission ice = new Permission("ice");
        Permission msi = new Permission("msi");
        Permission assistance = new Permission("assistance");

        shipDao.saveEntity(ais);
        shipDao.saveEntity(ice);
        shipDao.saveEntity(msi);
        shipDao.saveEntity(assistance);

        ShoreRole role = new ShoreRole();
        role.add(ais);
        role.add(ice);
        role.add(msi);
        role.add(assistance);

        shipDao.saveEntity(role);

        SecuredUser user = new SecuredUser("iceCenter", "qwerty", "obo@dma.dk");
        user.addRole(role);

        shipDao.saveEntity(user);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    private void createGreenposReports() {
        DateTimeConverter converter = DateTimeConverter.getDateTimeConverter();
        Ship ship = shipDao.getShipByCallsign("OXPJ2");

        List<ReportedVoyage> voyages = null;
        GreenPosReport report = new GreenPosSailingPlanReport(ship.getName(), ship.getMmsi(), ship.getCallsign(),
                ship.getMaritimeId(), new Position("66 56.5N", "053 40.50W"), "Sun shine", "NO ICE", 4.1, 10, "Nuuk",
                converter.toObject("19-09-2013 10:30"), 6, voyages);
        report.setReportedBy("oratank");
        report.setTs(converter.toObject("18-09-2013 13:09"));
        shipDao.saveEntity(report);

        report = new GreenPosPositionReport(ship.getName(), ship.getMmsi(), ship.getCallsign(), ship.getMaritimeId(),
                new Position("66 03.772N", "053 46.3W"), "Sun shine", "NO ICE", 10.0, 10);
        report.setReportedBy("oratank");
        report.setTs(converter.toObject("18-09-2013 18:00"));
        shipDao.saveEntity(report);

        report = new GreenPosPositionReport(ship.getName(), ship.getMmsi(), ship.getCallsign(), ship.getMaritimeId(),
                new Position("65 19.926N", "052 57.483W"), "Sun shine", "NO ICE", 10.0, 10);
        report.setReportedBy("oratank");
        report.setTs(converter.toObject("19-09-2013 00:00"));
        shipDao.saveEntity(report);

        report = new GreenPosPositionReport(ship.getName(), ship.getMmsi(), ship.getCallsign(), ship.getMaritimeId(),
                new Position("64 29.198N", "052 29.507W"), "Sun shine", "NO ICE", 10.0, 10);
        report.setReportedBy("oratank");
        report.setTs(converter.toObject("19-09-2013 06:00"));
        shipDao.saveEntity(report);

        report = new GreenPosFinalReport(ship.getName(), ship.getMmsi(), ship.getCallsign(), ship.getMaritimeId(),
                new Position("64 10.4N", "051 43.5W"), "Sun shine", "NO ICE");
        report.setReportedBy("oratank");
        report.setTs(converter.toObject("19-09-2013 10:15"));
        shipDao.saveEntity(report);

        ship = shipDao.getShipByCallsign("OYDK2");

        report = new GreenPosPositionReport(ship.getName(), ship.getMmsi(), ship.getCallsign(), ship.getMaritimeId(),
                new Position("63 80.01N", "051 58.04W"), "Sun shine", "NO ICE", 11.6, 350);
        report.setReportedBy("orasila");
        report.setTs(converter.toObject("24-09-2013 12:00"));
        shipDao.saveEntity(report);

        report = new GreenPosFinalReport(ship.getName(), ship.getMmsi(), ship.getCallsign(), ship.getMaritimeId(),
                new Position("64 10.4N", "051 43.5W"), "Sun shine", "NO ICE");
        report.setReportedBy("orasila");
        report.setTs(converter.toObject("24-09-2013 16:02"));
        shipDao.saveEntity(report);

        voyages = null;
        report = new GreenPosSailingPlanReport(ship.getName(), ship.getMmsi(), ship.getCallsign(),
                ship.getMaritimeId(), new Position("64 10.4N", "051 43.5W"), "Sun shine", "NO ICE", 4.1, 150,
                "KYSTFART", converter.toObject("26-09-2013 10:30"), 6, voyages);
        report.setReportedBy("orasila");
        report.setTs(converter.toObject("24-09-2013 23:12"));
        shipDao.saveEntity(report);

        report = new GreenPosPositionReport(ship.getName(), ship.getMmsi(), ship.getCallsign(), ship.getMaritimeId(),
                new Position("64 10.068N", "051 64.78W"), "Sun shine", "NO ICE", 11.6, 162);
        report.setReportedBy("orasila");
        report.setTs(converter.toObject("25-09-2013 00:00"));
        shipDao.saveEntity(report);

    }

    public void logExistingEntries() {
        logger.info("Permissions: {} ", shipDao.getAll(Permission.class));
        logger.info("Roles: {} ", shipDao.getAll(Role.class));
        logger.info("Users: {} ", shipDao.getAll(SecuredUser.class));
        logger.info("Ships: {} ", shipDao.getAll(Ship.class));
        logger.info("VoyagePlans: {} ", shipDao.getAll(VoyagePlan.class));
        logger.info("Voyage: {} ", shipDao.getAll(Voyage.class));
        logger.info("Berth: {} ", shipDao.getAll(Berth.class));
    }

    private void insertDemoRoute(String voyageId, String file, boolean activate) {
        InputStream is = getClass().getResourceAsStream(file);
        try {
            Route r = shipService.parseRoute(is);
            shipService.saveRoute(r, voyageId, activate);
        } catch (IOException e) {
            logger.error("Failed uploading demo route Miami-Nuuk.txt", e);
        }
    }

}
