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
import org.slf4j.Logger;

import dk.dma.arcticweb.dao.ShipDao;
import dk.dma.embryo.domain.AuthorityRole;
import dk.dma.embryo.domain.Berth;
import dk.dma.embryo.domain.GreenPosReport;
import dk.dma.embryo.domain.IEntity;
import dk.dma.embryo.domain.Permission;
import dk.dma.embryo.domain.Role;
import dk.dma.embryo.domain.Route;
import dk.dma.embryo.domain.Sailor;
import dk.dma.embryo.domain.SecuredUser;
import dk.dma.embryo.domain.Ship;
import dk.dma.embryo.domain.Voyage;
import dk.dma.embryo.domain.VoyagePlan;

@Singleton
@Startup
public class TestServiceBean {

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
        createOraTestData();
        uploadOraRoutes();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    private void createOraTestData() {
        logger.info("BEFORE CREATION - ORASILA");

        // Create ship and user
        Ship newShip = new Ship();
        newShip.setName("ORASILA");
        newShip.setMmsi(220443000L);
        newShip.setCallsign("OYDK2");
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

        SecuredUser user = new SecuredUser("ora", "qwerty", "obo@dma.dk");
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
                now.minusDays(3).withTime(17, 0, 0, 0)));
        voyagePlan.addVoyageEntry(new Voyage("Nuuk", "64 10.4N", "051 43.5W", now.plusDays(3).withTime(10, 30, 0, 0),
                now.plusDays(5).withTime(9, 0, 0, 0)));
        voyagePlan.addVoyageEntry(new Voyage("Thule", "77 27.8N", "069 14.0W", now.plusDays(9).withTime(13, 15, 0, 0),
                now.plusDays(11).withTime(9, 0, 0, 0)));
        voyagePlan.addVoyageEntry(new Voyage("Upernavik", "72 47.5N", "056 09.4W", now.plusDays(13).withTime(10, 45, 0,
                0), now.plusDays(14).withTime(9, 30, 0, 0)));

        shipDao.saveEntity(voyagePlan);

    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    private void uploadOraRoutes() {
        logger.info("BEFORE UPLOAD - ORASILA");

        VoyagePlan voyagePlan = shipService.getVoyagePlan(220443000L);
        insertDemoRoute(voyagePlan.getVoyagePlan().get(0).getEnavId(), "/demo/routes/Miami-Nuuk.txt", true);
        insertDemoRoute(voyagePlan.getVoyagePlan().get(1).getEnavId(), "/demo/routes/Nuuk-Thule.txt", false);
        insertDemoRoute(voyagePlan.getVoyagePlan().get(2).getEnavId(), "/demo/routes/Thule-Upernavik.txt", false);
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
