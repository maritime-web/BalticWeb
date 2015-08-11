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
package dk.dma.arcticweb.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import dk.dma.arcticweb.reporting.model.GreenPosDeviationReport;
import dk.dma.arcticweb.reporting.model.GreenPosFinalReport;
import dk.dma.arcticweb.reporting.model.GreenPosPositionReport;
import dk.dma.arcticweb.reporting.model.GreenPosReport;
import dk.dma.arcticweb.reporting.model.GreenPosSailingPlanReport;
import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.persistence.IEntity;
import dk.dma.embryo.common.util.DateTimeConverter;
import dk.dma.embryo.user.model.AdministratorRole;
import dk.dma.embryo.user.model.ReportingAuthorityRole;
import dk.dma.embryo.user.model.Role;
import dk.dma.embryo.user.model.SailorRole;
import dk.dma.embryo.user.model.SecuredUser;
import dk.dma.embryo.user.model.ShoreRole;
import dk.dma.embryo.user.persistence.RealmDao;
import dk.dma.embryo.user.security.SecurityUtil;
import dk.dma.embryo.vessel.component.RouteParserComponent;
import dk.dma.embryo.vessel.component.RouteSaver;
import dk.dma.embryo.vessel.model.Berth;
import dk.dma.embryo.vessel.model.Position;
import dk.dma.embryo.vessel.model.Route;
import dk.dma.embryo.vessel.model.Vessel;
import dk.dma.embryo.vessel.model.Voyage;
import dk.dma.embryo.vessel.persistence.ScheduleDao;
import dk.dma.embryo.vessel.persistence.VesselDao;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
@Startup
public class AppDataServiceBean {
    
    private static final String EXCLUDE_EXACT_EARTH = "ExcludeExactEarth";
    
    @Inject
    private RealmDao realmDao;

    @EJB
    private VesselDao vesselDao;

    @EJB
    private ScheduleDao scheduleDao;

    @Inject
    private Logger logger;

    @Inject
    private EntityManagerFactory emf;

    @Inject
    private EntityManager em;

    private List<Berth> berthList = new ArrayList<>(200);

    @Property("embryo.users.admin.initial.pw")
    @Inject
    private String dmaInitialPw;

    @Property("embryo.users.admin.initial.email")
    @Inject
    private String dmainitialEmail;

    @Property("embryo.users.test.password")
    @Inject
    private String testPassword;

    @Property("embryo.users.test.email")
    @Inject
    private String testEmail;


    @PostConstruct
    public void startup() {
        Map<String, Object> props = emf.getProperties();

        String hbm2dllAuto = (String) props.get("hibernate.hbm2ddl.auto");
        logger.info("Detected database auto update setting: {}", hbm2dllAuto);

        setupBerthList();

        if ("create-drop".equals(hbm2dllAuto)) {
            createAdmin();
            resetBaseData();
            resetTestData();
        } else if ("update".equals(hbm2dllAuto)) {
            createAdmin();
            resetBaseData();
        } else if ("validate".equals(hbm2dllAuto)) {
            createAdmin();
            resetBaseData();
        }
    }

    private void setupBerthList() {

        berthList
                .add(new Berth("Ikerasassuaq Vejrstation", "Prins Christian Sund Vejrstation", "60 03.5N", "043 10.5W"));
        berthList.add(new Berth("Aappilattoq", null, "60 09.6N", "044 17.2W"));
        berthList.add(new Berth("Narsaq Kujalleq", "Frederiksdal", "60 00.4N", "044 40.0W"));
        berthList.add(new Berth("Tasiusaq", null, "60 11.8N", "044 49.5W"));
        berthList.add(new Berth("Nanortalik", null, "60 08.3N", "045 14.5W"));
        berthList.add(new Berth("Alluitsup Paa", "Sydprøven", "60 27.8N", "045 34.2W"));
        berthList.add(new Berth("Alluitsoq", "Lichtenau", "60 30.6N", "045 32.5W"));
        berthList.add(new Berth("Ammassivik", "Sletten", "60 35.8N", "045 23.7W"));
        berthList.add(new Berth("Qallimiut", null, "60 42.5N", "045 22.0W"));
        berthList.add(new Berth("Saarloq", null, "60 32.3N", "046 01.9W"));
        berthList.add(new Berth("Eqalugaarsuit", null, "60 37.4N", "045 54.8W"));
        berthList.add(new Berth("Qaqortoq", "Julianehåb", "60 43.1N", "046 02.4W"));
        berthList.add(new Berth("Upernaviarsuk", null, "60 45.0N", "045 53.8W"));
        berthList.add(new Berth("Qassimiut", null, "60 46.9N", "047 09.8W"));
        berthList.add(new Berth("Igaliku", null, "60 59.6N", "045 25.4W"));
        berthList.add(new Berth("Narsaq", null, "60 54.5N", "046 03.0W"));
        berthList.add(new Berth("Narsarsuaq", null, "61 08.8N", "045 26.2W"));
        berthList.add(new Berth("Qassiarsuk", null, "61 09.3N", "045 31.0W"));
        berthList.add(new Berth("Kangilinnguit", "Grønnedal", "61 14.3N", "48 06.1W"));
        berthList.add(new Berth("Arsuk", null, "61 10.5N", "048 27.2W"));
        berthList.add(new Berth("Paamiut", "Frederikshåb", "61 59.8N", "049 40.8W"));
        berthList.add(new Berth("Qeqertarsuatsiaat", "Fiskenæsset", "63 05.4N", "050 41.0W"));
        berthList.add(new Berth("Orsiivik", "Polaroil", "63 41.1N", "051 30.6W"));
        berthList.add(new Berth("Nordafar", null, "63 42.0N", "051 29.6W"));
        berthList.add(new Berth("Nuuk", "Godthåb", "64 10.4N", "051 43.5W"));
        berthList.add(new Berth("Kapisillit", null, "64 26.0N", "050 16.0W"));
        berthList.add(new Berth("Atammik", null, "64 48.3N", "052 11.0W"));
        berthList.add(new Berth("Napasoq", null, "65 02.7N", "052 23.1W"));
        berthList.add(new Berth("Maniitsoq", "Sukkertoppen", "65 24.8N", "052 54.3W"));
        berthList.add(new Berth("Kangaamiut", null, "65 49.6N", "053 20.9W"));
        berthList.add(new Berth("Kangerlussuup Umiarsualivia", null, "66 58.2N", "050 57.2W"));
        berthList.add(new Berth("Itilleq", null, "66 34.6N", "053 30.2W"));
        berthList.add(new Berth("Sarfannguit", null, "66 54.0N", "052 52.0W"));
        berthList.add(new Berth("Sisimiut", "Holsteinsborg", "66 56.5N", "053 40.5W"));
        berthList.add(new Berth("Attu", null, "67 56.5N", "053 38.0W"));
        berthList.add(new Berth("Ikerasaarsuk", null, "68 08.4N", "053 27.2W"));
        berthList.add(new Berth("Iginniarfik", null, "68 09.0N", "053 10.8W"));
        berthList.add(new Berth("Niaqornaarsuk", null, "68 14.0N", "052 52.0W"));
        berthList.add(new Berth("Kangaatsiaq", null, "68 18.5N", "053 28.0W"));
        berthList.add(new Berth("Aasiaat", "Egedesminde", "68 42.6N", "052 53.0W"));
        berthList.add(new Berth("Kitsissuarsuit", "Hunde Ejland", "68 51.5N", "053 07.8W"));
        berthList.add(new Berth("Akunnaaq", null, "68 44.7N", "052 20.2W"));
        berthList.add(new Berth("Qeqertarsuaq", "Godhavn", "69 15.0N", "053 33.0W"));
        berthList.add(new Berth("Kangerluk", "Diskofjord", "69 29.0N", "053 57.0W"));
        berthList.add(new Berth("Ikamiut", null, "68 38.1N", "051 50.0W"));
        berthList.add(new Berth("Qasigiannguit", "Christianshåb", "68 49.0N", "051 12.0W"));
        berthList.add(new Berth("Ilimanaq", "Claushavn", "69 05.0N", "051 07.3W"));
        berthList.add(new Berth("Ilulissat", "Jakobshavn", "69 13.5N", "051 06.0W"));
        berthList.add(new Berth("Oqaatsut", "Rodebay", "69 20.7N", "051 00.7W"));
        berthList.add(new Berth("Qeqertaq", null, "70 00.0N", "051 19.0W"));
        berthList.add(new Berth("Saqqaq", null, "70 01.0N", "051 57.0W"));
        berthList.add(new Berth("Niaqornat", null, "70 47.6N", "053 40.0W"));
        berthList.add(new Berth("Qaarsut", null, "70 44.0N", "052 38.5W"));
        berthList.add(new Berth("Uummannaq", null, "70 40.5N", "052 08.0W"));
        berthList.add(new Berth("Ikerasak", null, "70 30.0N", "051 19.0W"));
        berthList.add(new Berth("Saattut", null, "70 48.8N", "051 38.2W"));
        berthList.add(new Berth("Ukkusissat", null, "71 03.0N", "051 53.8W"));
        berthList.add(new Berth("Illorsuit", null, "71 14.3N", "053 30.5W"));
        berthList.add(new Berth("Nuugaatsiaq", null, "71 32.5N", "053 13.0W"));
        berthList.add(new Berth("Upernavik Kujalleq", "Søndre Upernavik", "72 09.2N", "055 32.0W"));
        berthList.add(new Berth("Kangersuatsiaq", "Prøven", "72 22.7N", "055 33.5W"));
        berthList.add(new Berth("Upernavik", null, "72 47.5N", "056 09.4W"));
        berthList.add(new Berth("Aappilattoq", null, "72 53.0N", "055 36.6W"));
        berthList.add(new Berth("Naajaat", null, "73 09.0N", "055 48.0W"));
        berthList.add(new Berth("Innaarsuit", null, "73 12.0N", "056 02.0W"));
        berthList.add(new Berth("Tasiusaq", null, "73 22.0N", "056 04.0W"));
        berthList.add(new Berth("Nutaarmiut", null, "73 31.8N", "056 26.0W"));
        berthList.add(new Berth("Nuussuaq", "Kraulshavn", "74 07.0N", "057 06.0W"));
        berthList.add(new Berth("Kullorsuaq", null, "74 34.8N", "057 13.0W"));
        berthList.add(new Berth("Savissivik", null, "76 01.0N", "065 06.0W"));
        berthList.add(new Berth("Pituffik", "Thule Air Base", "76 32.6N", "068 52.5W"));
        berthList.add(new Berth("Moriusaq", null, "76 45.0N", "069 53.0W"));
        berthList.add(new Berth("Qeqertarsuaq", "Herbert Ø", "77 25.3N", "070 09.0W"));
        berthList.add(new Berth("Qaanaaq", "Thule", "77 27.8N", "069 14.0W"));
        berthList.add(new Berth("Qeqertat", null, "77 29.8N", "066 43.0W"));
        berthList.add(new Berth("Siorapaluk", null, "77 47.0N", "070 37.0W"));
        berthList.add(new Berth("Danmarkshavn", null, "76 46.0N", "018 45.0W"));
        berthList.add(new Berth("Daneborg", null, "74 18.2N", "020 14.7W"));
        berthList.add(new Berth("Ella Ø", null, "72 53.0N", "025 06.0W"));
        berthList.add(new Berth("Nyhavn", null, "72 16.0N", "023 57.0W"));
        berthList.add(new Berth("Illoqqortoormiut", "Scoresbysund", "70 28.0N", "021 58.0W"));
        berthList.add(new Berth("Ittaajimmiut", "Kap Hope", "70 27.5N", "022 22.0W"));
        berthList.add(new Berth("Uunarteq", "Kap Tobin", "70 24.5N", "021 58.0W"));
        berthList.add(new Berth("Nerlerit Inaat", "Constable Pynt", "70 44.0N", "022 38.0W"));
        berthList.add(new Berth("Sermiligaaq", null, "65 54.0N", "036 22.0W"));
        berthList.add(new Berth("Tiniteqilaaq", null, "65 52.9N", "037 46.9W"));
        berthList.add(new Berth("Kuummiut", null, "65 51.5N", "037 00.5W"));
        berthList.add(new Berth("Ikkatteq", null, "65 37.8N", "037 57.2W"));
        berthList.add(new Berth("Tasiilaq", null, "65 36.5N", "037 37.5W"));
        berthList.add(new Berth("Kulusuk", "Kap Dan", "65 34.3N", "037 11.0W"));
        berthList.add(new Berth("Kulusuk Mittarfik", "Kulusuk Flyveplads", "65 34.7N", "037 08.7W"));
        berthList.add(new Berth("Isortoq", null, "65 32.4N", "038 58.5W"));
        berthList.add(new Berth("Saqqisikuik", "Skjoldungen", "63 13.0N", "041 24.0W"));
        berthList.add(new Berth("Seqinnersuusaq", null, "64 58.7N", "051 34.9W"));
        berthList.add(new Berth("Maarmorilik", null, "71 07.6N", "051 16.5W"));
        berthList.add(new Berth("Zackenberg Forskningsstation", null, "74 28.0N", "020 34.0W"));
        berthList.add(new Berth("Aalborg", null, "57 02.9N", "010 03.3E"));

        addBerthsFromFiles(berthList);
    }

    private void addBerthsFromFiles(List<Berth> berthList) {
        if (berthList == null) {
            berthList = new ArrayList<>();
        }
        InputStream berthStream = getClass().getResourceAsStream("/berths/berths_no.json");
        try {
            JsonParser jsonParser = new JsonFactory().createParser(berthStream);
            String currentName = null;
            double currentLat = 0;
            double currentLon = 0;
            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                String name = jsonParser.getCurrentName();
                JsonToken currentToken = jsonParser.getCurrentToken();
                if ("name".equals(name)) {
                    jsonParser.nextToken();
                    currentName = jsonParser.getText();
                }
                if ("lat".equals(name)) {
                    jsonParser.nextToken();
                    currentLat = jsonParser.getDoubleValue();
                }
                if ("lon".equals(name)) {
                    jsonParser.nextToken();
                    currentLon = jsonParser.getDoubleValue();
                }
                if (currentToken == JsonToken.END_OBJECT) {
                    Position position = new Position(currentLat, currentLon);
                    Berth berth = new Berth(currentName, position.getLatitudeAsString(), position.getLongitudeAsString());
                    berthList.add(berth);
                    currentName = null;
                    currentLat = 0;
                    currentLon = 0;
                }
            }
            berthStream.close();
        } catch (JsonParseException e) {
            logger.error("JSON parsing exception when importing berths from file", e);
        } catch (IOException e) {
            logger.error("I/O exception when importing berths from file", e);
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void resetBaseData() {
        deleteAll(Berth.class);
        createBerths();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void createAdmin() {
        SecuredUser user = realmDao.findByUsername("dma");
        logger.debug("looked up admin user: {}", user);
        if (user == null) {
            createDmaAccount();
        } else {
            logger.info("Admin user (dma) already exists in database");
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void resetTestData() {
        internalResetTestData();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void clearTestData() {
        logger.info("Deleting existing test data entries");

        // delete any other voyages
        deleteAll(Voyage.class);
        deleteAll(Vessel.class);
        deleteAll(Route.class);
        clearTestUsers();
        deleteAll(Vessel.class);
        deleteAll(GreenPosReport.class);

        logger.info("AFTER DELETION");
    }

    private void internalResetTestData() {
        clearTestData();

        createOrasilaTestData();
        uploadOrasilaRoutes();
        createOraTankTestData();
        uploadOraTankRoutes();
        createSarfaqTestData();
        createSarfaqSchedule();
        //uploadSarfaqRoutes();
        createNajaArcticaTestData();
        createArinaArcticaTestData();

        createSilverExplorerTestData();
        createArtaniaTestData();
        createEmeraldPrincessTestData();
        // createCarnivalLegendTestData();
        // uploadCarnivalLegendRoutes();
        createDmiLogin();
        createArcticCommandLogin();
        createAasiaatLogin();
        createNanoqLogin();
        createGreenposReports();
    }

    private <E extends IEntity<K>, K> void deleteAll(Class<E> type) {
        try {
            logger.info("Deleting entities of type {}", type.getName());
            List<E> entities = vesselDao.getAll(type);
            for (E entity : entities) {
                logger.info("Deleting entities with id {}", entity.getId());
                vesselDao.remove(entity);
            }
            em.flush();
            em.clear();
        } catch (RuntimeException e) {
            logger.error("Error deleting existing entries", e);
            throw e;
        }
    }

    public void clearTestUsers() {
        clearUsers(false);
    }

    private void clearUsers(boolean dma) {
        try {
            logger.info("Deleting test users (entities of type{})", SecuredUser.class.getName());

            List<SecuredUser> entities = vesselDao.getAll(SecuredUser.class);
            for (SecuredUser user : entities) {
                if (dma && "dma".equals(user.getUserName()) || !(dma || "dma".equals(user.getUserName()))) {
                    logger.info("Deleting entities with id {}", user.getId());
                    vesselDao.remove(user);
                }
            }
            em.flush();
            em.clear();
        } catch (RuntimeException e) {
            logger.error("Error deleting existing entries", e);
            throw e;
        }
    }

    private void createDmaAccount() {
        logger.info("BEFORE CREATION - DMA");

        AdministratorRole auth = new AdministratorRole();
        realmDao.saveEntity(auth);

        SecuredUser user = SecurityUtil.createUser("dma", dmaInitialPw, dmainitialEmail, null);
        user.setRole(auth);
        SecuredUser saved = realmDao.saveEntity(user);

        em.flush();
        em.clear();
        logger.info("SAVED DMA: " + saved);
    }

    private void createBerths() {
        logger.info("BEFORE CREATION - Berths");

        for (Berth berth : berthList) {
            vesselDao.saveEntity(berth);
        }

        em.flush();
        em.clear();
    }

    private void createOrasilaTestData() {
        logger.info("BEFORE CREATION - ORASILA");

        // Create vessel and user
        Vessel newVessel = new Vessel();
        newVessel.setMmsi(220443000L);
        newVessel.setCommCapabilities("Sat C 0581 422044310, GMDSS A1+A2+A3");
        newVessel.setMaxSpeed(BigDecimal.valueOf(12.6));
        newVessel.setGrossTonnage(2194);
        newVessel.setIceClass("A1");
        newVessel.setHelipad(Boolean.FALSE);
        newVessel.getAisData().setImoNo(9336725L);
        newVessel.getAisData().setName("ORASILA");
        newVessel.getAisData().setCallsign("OYDK2");

        newVessel = vesselDao.saveEntity(newVessel);

        SailorRole sailorRole = new SailorRole();
        sailorRole.setVessel(newVessel);

        vesselDao.saveEntity(sailorRole);

        SecuredUser user = SecurityUtil.createUser("orasila", testPassword, testEmail, null);
        user.setRole(sailorRole);

        vesselDao.saveEntity(user);

        DateTime now = DateTime.now(DateTimeZone.UTC);

        newVessel.addVoyageEntry(new Voyage("Is never shown", "25 47.16N", "080 22.410W", null, now.minusDays(6)
                .withTime(17, 0, 0, 0), 0, 0, false));
        newVessel.addVoyageEntry(new Voyage("Miami", "25 47.16N", "080 13.27W", now.minusDays(4).withTime(9, 30, 0, 0),
                now.minusDays(3).withTime(17, 0, 0, 0), 12, 0, true));
        newVessel.addVoyageEntry(new Voyage("Nuuk", "64 10.4N", "051 43.5W", now.plusDays(3).withTime(10, 30, 0, 0),
                now.plusDays(5).withTime(9, 0, 0, 0), 12, 0, true));
        newVessel.addVoyageEntry(new Voyage("Thule", "77 27.8N", "069 14.0W", now.plusDays(9).withTime(13, 15, 0, 0),
                now.plusDays(11).withTime(9, 0, 0, 0)));
        newVessel.addVoyageEntry(new Voyage("Upernavik", "72 47.5N", "056 09.4W", now.plusDays(13).withTime(10, 45, 0,
                0), now.plusDays(14).withTime(9, 30, 0, 0)));

        for (Voyage v : newVessel.getSchedule()) {
            vesselDao.saveEntity(v);
        }
    }

    private void uploadOrasilaRoutes() {
        logger.info("BEFORE UPLOAD - ORASILA");

        List<Voyage> schedule = scheduleDao.getSchedule(220443000L);
        insertDemoRoute(schedule.get(0).getEnavId(), "/demo/routes/Miami-Nuuk.txt", true);
        insertDemoRoute(schedule.get(1).getEnavId(), "/demo/routes/Nuuk-Thule.txt", false);
        insertDemoRoute(schedule.get(2).getEnavId(), "/demo/routes/Thule-Upernavik.txt", false);
    }

    private void createOraTankTestData() {
        logger.info("BEFORE CREATION - ORATANK");

        // Create vessel and user
        Vessel newVessel = new Vessel();
        newVessel.getAisData().setName("ORATANK");
        newVessel.setMmsi(220516000L);
        newVessel.getAisData().setCallsign("OXPJ2");
        newVessel = vesselDao.saveEntity(newVessel);

        SailorRole sailorRole = new SailorRole();
        sailorRole.setVessel(newVessel);

        vesselDao.saveEntity(sailorRole);

        SecuredUser user = SecurityUtil.createUser("oratank", testPassword, testEmail, null);
        user.setRole(sailorRole);

        vesselDao.saveEntity(user);

        DateTime now = DateTime.now(DateTimeZone.UTC);

        newVessel.addVoyageEntry(new Voyage("Nuuk", "64 10.4N", "051 43.5W", now.plusDays(3).withTime(10, 30, 0, 0),
                now.plusDays(5).withTime(9, 0, 0, 0)));
        newVessel.addVoyageEntry(new Voyage("X", "63 41.81N", "051 29.00W", now.minusDays(4).withTime(9, 30, 0, 0), now
                .minusDays(3).withTime(17, 0, 0, 0)));

        for (Voyage v : newVessel.getSchedule()) {
            vesselDao.saveEntity(v);
        }
    }

    private void uploadOraTankRoutes() {
        logger.info("BEFORE UPLOAD - ORATANK");

        List<Voyage> schedule = scheduleDao.getSchedule(220516000L);
        insertDemoRoute(schedule.get(0).getEnavId(), "/demo/routes/Oratank-Nuuk.txt", true);
    }

    private void createSarfaqTestData() {
        logger.info("BEFORE CREATION - SARFAQ ITTUK");

        // Create vessel and user
        Vessel newVessel = new Vessel();
        newVessel.setMmsi(331037000L);
        newVessel = vesselDao.saveEntity(newVessel);

        SailorRole sailorRole = new SailorRole();
        sailorRole.setVessel(newVessel);

        vesselDao.saveEntity(sailorRole);

        SecuredUser user = SecurityUtil.createUser("sarfaq", testPassword, testEmail, null);
        user.setRole(sailorRole);

        vesselDao.saveEntity(user);

    }

    private void createSarfaqSchedule() {
        logger.info("BEFORE SCHEDULE - SARFAQ");

        Vessel sarfaq = vesselDao.getVessel(331037000L);

        DateTime now = DateTime.now(DateTimeZone.UTC);

        DateTimeConverter converter = DateTimeConverter.getDateTimeConverter();

        DateTime firstDeparture = converter.toObject("27-09-2013 21:00");

        sarfaq.addVoyageEntry(new Voyage("Nuuk", "64 10.4N", "051 43.5W", null, firstDeparture));
        sarfaq.addVoyageEntry(new Voyage("Maniitsoq", "65 24.8N", "052 54.3W", converter.toObject("28-09-2013 07:00"),
                converter.toObject("28-09-2013 07:30")));
        sarfaq.addVoyageEntry(new Voyage("Kangaamiut", "65 49.6N", "053 20.9W", converter.toObject("28-09-2013 10:45"),
                converter.toObject("28-09-2013 11:00")));
        sarfaq.addVoyageEntry(new Voyage("Sisimiut", "66 56.5N", "053 40.5W", converter.toObject("28-09-2013 18:00"),
                converter.toObject("28-09-2013 21:00")));
        sarfaq.addVoyageEntry(new Voyage("Aasiaat", "68 42.6N", "052 53.0W", converter.toObject("29-09-2013 08:00"),
                converter.toObject("29-09-2013 08:30")));
        sarfaq.addVoyageEntry(new Voyage("Ilulissat", "69 13.5N", "051 06.0W", converter.toObject("29-09-2013 13:00"),
                converter.toObject("29-09-2013 17:00")));
        sarfaq.addVoyageEntry(new Voyage("Aasiaat", "68 42.6N", "052 53.0W", converter.toObject("29-09-2013 21:30"),
                converter.toObject("29-09-2013 22:00")));
        sarfaq.addVoyageEntry(new Voyage("Sisimiut", "66 56.5N", "053 40.5W", converter.toObject("30-09-2013 09:00"),
                converter.toObject("30-09-2013 10:30")));
        sarfaq.addVoyageEntry(new Voyage("Kangaamiut", "65 49.6N", "053 20.9W", converter.toObject("30-09-2013 17:30"),
                converter.toObject("30-09-2013 17:45")));
        sarfaq.addVoyageEntry(new Voyage("Maniitsoq", "65 24.8N", "052 54.3W", converter.toObject("30-09-2013 21:30"),
                converter.toObject("30-09-2013 22:00")));
        sarfaq.addVoyageEntry(new Voyage("Nuuk", "64 10.4N", "051 43.5W", converter.toObject("01-10-2013 06:30"),
                converter.toObject("01-10-2013 09:00")));
        sarfaq.addVoyageEntry(new Voyage("Qeqertarsuatsiaat", "63 05.4N", "050 41.0W", converter
                .toObject("01-10-2013 16:30"), converter.toObject("01-10-2013 16:45")));

        sarfaq.addVoyageEntry(new Voyage("Paamiut", "61 59.8N", "049 40.8W", converter.toObject("01-10-2013 23:30"),
                converter.toObject("02-10-2013 00:00")));
        sarfaq.addVoyageEntry(new Voyage("Arsuk", "61 10.5N", "048 27.1W", converter.toObject("02-10-2013 06:45"),
                converter.toObject("02-10-2013 07:00")));
        sarfaq.addVoyageEntry(new Voyage("Qaqortoq", "60 43.1N", "046 02.4W", converter.toObject("02-10-2013 15:30"),
                converter.toObject("02-10-2013 19:00")));
        sarfaq.addVoyageEntry(new Voyage("Narsaq", "60 54.5N", "046 03.0W", converter.toObject("02-10-2013 21:00"),
                converter.toObject("02-10-2013 21:30")));

        sarfaq.addVoyageEntry(new Voyage("Arsuk", "61 10.5N", "048 27.2W", converter.toObject("03-10-2013 06:45"),
                converter.toObject("03-10-2013 07:00")));
        sarfaq.addVoyageEntry(new Voyage("Paamiut", "61 59.8N", "049 40.8W", converter.toObject("03-10-2013 13:30"),
                converter.toObject("03-10-2013 14:30")));
        sarfaq.addVoyageEntry(new Voyage("Qeqertarsuatsiaat", "63 05.4N", "050 41.0W", converter
                .toObject("03-10-2013 22:30"), converter.toObject("03-10-2013 22:45")));
        sarfaq.addVoyageEntry(new Voyage("Nuuk", "64 10.4N", "051 43.5W", converter.toObject("04-10-2013 09:00"),
                converter.toObject("04-10-2013 11:00")));

        // firstDeparture.

        Duration d = new Duration(firstDeparture, now);
        int weeks = (int) d.getStandardDays() / 7;

        logger.debug("Duration: {}, weeks:{}, days: {}", d, weeks, d.getStandardDays());

        List<Voyage> schedule = sarfaq.getSchedule();
        if (weeks > 0) {
            for (Voyage v : schedule) {
                v.setArrival(v.getArrival() == null ? null : v.getArrival().plusWeeks(weeks));
                v.setDeparture(v.getDeparture() == null ? null : v.getDeparture().plusWeeks(weeks));
            }
        }
        logger.debug("Schedule: {}", schedule);

        for (Voyage v : schedule) {
            scheduleDao.saveEntity(v);
        }
        em.flush();
    }

    private void uploadSarfaqRoutes() {
        logger.info("BEFORE UPLOAD - SARFAQ");

        List<Voyage> schedule = scheduleDao.getSchedule(331037000L);

        logger.debug("schedule: {}", schedule);

        insertDemoRoute(schedule.get(0).getEnavId(), "/demo/routes/SARFAQ-Nuuk-Maniitsoq.txt", true);
        insertDemoRoute(schedule.get(1).getEnavId(), "/demo/routes/SARFAQ-Maniitsoq-Kangaamiut.txt", false);
        insertDemoRoute(schedule.get(2).getEnavId(), "/demo/routes/SARFAQ-Kangaamiut-Sisimiut.txt", false);
    }

    private void createNajaArcticaTestData() {
        logger.info("BEFORE CREATION - NAJA ARCTICA");

        // Create vessel and user
        Vessel newVessel = new Vessel();
        newVessel.setMmsi(219623000L);
        newVessel = vesselDao.saveEntity(newVessel);

        SailorRole sailorRole = new SailorRole();
        sailorRole.setVessel(newVessel);

        vesselDao.saveEntity(sailorRole);

        SecuredUser user = SecurityUtil.createUser("naja", testPassword, testEmail, null);
        user.setRole(sailorRole);

        vesselDao.saveEntity(user);
    }

    private void createArinaArcticaTestData() {
        logger.info("BEFORE CREATION - ARINA ARCTICA");

        // Create vessel and user
        Vessel newVessel = new Vessel();
        newVessel.setMmsi(219097000L);
        newVessel = vesselDao.saveEntity(newVessel);

        SailorRole sailorRole = new SailorRole();
        sailorRole.setVessel(newVessel);

        vesselDao.saveEntity(sailorRole);

        SecuredUser user = SecurityUtil.createUser("arina", testPassword, testEmail, null);
        user.setRole(sailorRole);

        vesselDao.saveEntity(user);
    }

    private void createSilverExplorerTestData() {
        logger.info("BEFORE CREATION - SILVER EXPLORER");

        // Create vessel and user
        Vessel newVessel = new Vessel();
        newVessel.setMmsi(311562000L);
        newVessel = vesselDao.saveEntity(newVessel);

        SailorRole sailorRole = new SailorRole();
        sailorRole.setVessel(newVessel);

        vesselDao.saveEntity(sailorRole);

        SecuredUser user = SecurityUtil.createUser("silver", testPassword, testEmail, null);
        user.setRole(sailorRole);

        vesselDao.saveEntity(user);
    }

    private void createArtaniaTestData() {
        logger.info("BEFORE CREATION - ARTANIA");

        // Create vessel and user
        Vessel newVessel = new Vessel();
        newVessel.setMmsi(310456000L);
        newVessel = vesselDao.saveEntity(newVessel);

        SailorRole sailorRole = new SailorRole();
        sailorRole.setVessel(newVessel);

        vesselDao.saveEntity(sailorRole);

        SecuredUser user = SecurityUtil.createUser("artania", testPassword, testEmail, null);
        user.setRole(sailorRole);

        vesselDao.saveEntity(user);
    }

    private void createEmeraldPrincessTestData() {
        logger.info("BEFORE CREATION - EMERALD PRINCESS");

        // Create vessel and user
        Vessel newVessel = new Vessel();
        newVessel.setMmsi(310531000L);
        newVessel = vesselDao.saveEntity(newVessel);

        SailorRole sailorRole = new SailorRole();
        sailorRole.setVessel(newVessel);

        vesselDao.saveEntity(sailorRole);

        SecuredUser user = SecurityUtil.createUser("princess", testPassword, testEmail, null);
        user.setRole(sailorRole);

        vesselDao.saveEntity(user);
    }

    private void createCarnivalLegendTestData() {
        logger.info("BEFORE CREATION - CARNIVAL LEGEND");

        // Create vessel and user
        Vessel newVessel = new Vessel();
        newVessel.getAisData().setName("CARNIVAL LEGEND");
        newVessel.setMmsi(354237000L);
        newVessel.getAisData().setCallsign("H3VT");
        newVessel.getAisData().setImoNo(9224726L);
        newVessel.setGrossTonnage(85942);
        newVessel = vesselDao.saveEntity(newVessel);

        SailorRole sailorRole = new SailorRole();
        sailorRole.setVessel(newVessel);

        vesselDao.saveEntity(sailorRole);

        SecuredUser user = SecurityUtil.createUser("carnivalLegend", testPassword, testEmail, null);
        user.setRole(sailorRole);

        vesselDao.saveEntity(user);

        DateTime now = DateTime.now(DateTimeZone.UTC);

        newVessel.addVoyageEntry(new Voyage("Copenhagen", "55 67.61N", "12 56.83E", null, now.withTime(12, 57, 0, 0),
                12, 300, true));
        newVessel.addVoyageEntry(new Voyage("Nuuk", "64 10.4N", "051 43.5W", now.plusDays(10).withTime(7, 10, 0, 0),
                null));

        for (Voyage v : newVessel.getSchedule()) {
            vesselDao.saveEntity(v);
        }
    }

    private void uploadCarnivalLegendRoutes() {
        logger.info("BEFORE UPLOAD - CARNIVAL LEGEND");

        List<Voyage> schedule = scheduleDao.getSchedule(354237000L);
        insertDemoRoute(schedule.get(0).getEnavId(), "/demo/routes/CARNIVAL-LEGEND-Cph-Nuuk.txt", true);
    }

    private void createArcticCommandLogin() {
        logger.info("BEFORE CREATION - Arctic Command");

        ReportingAuthorityRole role = new ReportingAuthorityRole();

        vesselDao.saveEntity(role);

        SecuredUser user = SecurityUtil.createUser("arcticCommand", testPassword, testEmail, EXCLUDE_EXACT_EARTH);
        user.setRole(role);

        vesselDao.saveEntity(user);
    }

    private void createAasiaatLogin() {
        logger.info("BEFORE CREATION - Kyst radion");

        ReportingAuthorityRole role = new ReportingAuthorityRole();
        vesselDao.saveEntity(role);

        SecuredUser user = SecurityUtil.createUser("aasiaat", testPassword, testEmail, EXCLUDE_EXACT_EARTH);
        user.setRole(role);
        vesselDao.saveEntity(user);
    }

    private void createNanoqLogin() {
        logger.info("BEFORE CREATION - Nanoq");

        ShoreRole role = new ShoreRole();
        vesselDao.saveEntity(role);

        SecuredUser user = SecurityUtil.createUser("nanoq", testPassword, testEmail, EXCLUDE_EXACT_EARTH);
        user.setRole(role);
        vesselDao.saveEntity(user);
    }

    private void createDmiLogin() {
        logger.info("BEFORE CREATION - DMI");

        ShoreRole role = new ShoreRole();
        vesselDao.saveEntity(role);

        SecuredUser user = SecurityUtil.createUser("dmi", testPassword, testEmail, EXCLUDE_EXACT_EARTH);
        user.setRole(role);
        vesselDao.saveEntity(user);
    }

    private void createGreenposReports() {
        DateTimeConverter converter = DateTimeConverter.getDateTimeConverter();
        Vessel vessel = vesselDao.getVesselByCallsign("OXPJ2");

        DateTime now = DateTime.now(DateTimeZone.UTC);
        DateTime minus8 = now.minusDays(8);
        DateTime minus7 = now.minusDays(7);
        DateTime minus2 = now.minusDays(2);
        DateTime minus1 = now.minusDays(1);

        GreenPosReport report = new GreenPosSailingPlanReport(vessel.getAisData().getName(), vessel.getMmsi(), vessel
                .getAisData().getCallsign(), new Position("66 56.5N", "053 40.50W"), 1, "Sun shine", "NO ICE", 4.1, 10,
                "Nuuk", converter.toObject("19-09-2013 10:30"), 6, "Route with no particular good route description", null);
        report.setReportedBy("oratank");
        report.setRecipient("greenpos");
        report.setTs(minus8.withHourOfDay(13).withMinuteOfHour(9));
        vesselDao.saveEntity(report);

        report = new GreenPosPositionReport(vessel.getAisData().getName(), vessel.getMmsi(), vessel.getAisData()
                .getCallsign(), new Position("66 03.772N", "053 46.3W"), 2, "Sun shine", "NO ICE", 10.0, 10, null);
        report.setReportedBy("oratank");
        report.setRecipient("greenpos");
        report.setTs(minus8.withHourOfDay(18).withMinuteOfHour(0));
        vesselDao.saveEntity(report);

        report = new GreenPosPositionReport(vessel.getAisData().getName(), vessel.getMmsi(), vessel.getAisData()
                .getCallsign(), new Position("65 19.926N", "052 57.483W"), 3, "Sun shine", "NO ICE", 10.0, 10, null);
        report.setReportedBy("oratank");
        report.setRecipient("greenpos");
        report.setTs(minus7.withHourOfDay(0).withMinuteOfHour(0));
        vesselDao.saveEntity(report);

        report = new GreenPosPositionReport(vessel.getAisData().getName(), vessel.getMmsi(), vessel.getAisData()
                .getCallsign(), new Position("64 29.198N", "052 29.507W"), 4, "Sun shine", "NO ICE", 10.0, 10, null);
        report.setReportedBy("oratank");
        report.setRecipient("greenpos");
        report.setTs(minus7.withHourOfDay(6).withMinuteOfHour(0));
        vesselDao.saveEntity(report);

        report = new GreenPosFinalReport(vessel.getAisData().getName(), vessel.getMmsi(), vessel.getAisData()
                .getCallsign(), new Position("64 10.4N", "051 43.5W"), 5, "Sun shine", "NO ICE", null);
        report.setReportedBy("oratank");
        report.setRecipient("greenpos");
        report.setTs(minus7.withHourOfDay(10).withMinuteOfHour(15));
        vesselDao.saveEntity(report);

        vessel = vesselDao.getVesselByCallsign("OYDK2");

        report = new GreenPosPositionReport(vessel.getAisData().getName(), vessel.getMmsi(), vessel.getAisData()
                .getCallsign(), new Position("63 80.01N", "051 58.04W"), 2, "Sun shine", "NO ICE", 11.6, 350, null);
        report.setReportedBy("orasila");
        report.setRecipient("greenpos");
        report.setTs(minus2.withHourOfDay(12).withMinuteOfHour(0));
        vesselDao.saveEntity(report);

        report = new GreenPosFinalReport(vessel.getAisData().getName(), vessel.getMmsi(), vessel.getAisData()
                .getCallsign(), new Position("64 10.4N", "051 43.5W"), 3, "Sun shine", "NO ICE", null);
        report.setReportedBy("orasila");
        report.setRecipient("greenpos");
        report.setTs(minus2.withHourOfDay(16).withMinuteOfHour(2));
        vesselDao.saveEntity(report);

        report = new GreenPosSailingPlanReport(vessel.getAisData().getName(), vessel.getMmsi(), vessel.getAisData()
                .getCallsign(), new Position("64 10.4N", "051 43.5W"), 1, "Sun shine", "NO ICE", 4.1, 150, "KYSTFART",
                converter.toObject("26-09-2013 10:30"), 6, "Route with no particular good route description", null);
        report.setReportedBy("orasila");
        report.setRecipient("greenpos");
        report.setTs(minus2.withHourOfDay(23).withMinuteOfHour(12));
        vesselDao.saveEntity(report);

        report = new GreenPosPositionReport(vessel.getAisData().getName(), vessel.getMmsi(), vessel.getAisData()
                .getCallsign(), new Position("64 10.068N", "051 64.78W"), 2, "Sun shine", "Spredte skosser og let tyndis",
                11.6, 162, null);
        report.setReportedBy("orasila");
        report.setRecipient("greenpos");
        report.setTs(minus1.withHourOfDay(0).withMinuteOfHour(0));
        report.setTs(converter.toObject("25-09-2013 00:00"));
        vesselDao.saveEntity(report);

        report = new GreenPosDeviationReport(vessel.getAisData().getName(), vessel.getMmsi(), vessel.getAisData()
                .getCallsign(), new Position("64 10.068N", "051 64.78W"), 2,
                "Vi smutter lige en tur omkring Sisimiut og henter cigaretter mm. ", "Der er en udbredt mangel på cigaretter på broen, hvilket er et problem for sejladsen");
        report.setReportedBy("orasila");
        report.setRecipient("greenpos");
        report.setTs(minus1.withHourOfDay(4).withMinuteOfHour(0));
        vesselDao.saveEntity(report);

        report = new GreenPosPositionReport(vessel.getAisData().getName(), vessel.getMmsi(), vessel.getAisData()
                .getCallsign(), new Position("64 10.068N", "051 64.78W"), 3, "Sun shine", "Spredte skosser og let tyndis",
                11.6, 162, null);
        report.setReportedBy("orasila");
        report.setRecipient("greenpos");
        report.setTs(minus1.withHourOfDay(6).withMinuteOfHour(0));
        vesselDao.saveEntity(report);

    }

    private void logExistingEntries() {
        logger.info("Roles: {} ", vesselDao.getAll(Role.class));
        logger.info("Users: {} ", vesselDao.getAll(SecuredUser.class));
        logger.info("Vessels: {} ", vesselDao.getAll(Vessel.class));
        logger.info("Voyage: {} ", vesselDao.getAll(Voyage.class));
        logger.info("Berth: {} ", vesselDao.getAll(Berth.class));
    }

    private void insertDemoRoute(String voyageId, String file, boolean activate) {
        InputStream is = getClass().getResourceAsStream(file);
        try {
            Route r = new RouteParserComponent().parseRoute(file, is, new HashMap<String, String>());
            new RouteSaver(scheduleDao).saveRoute(r, voyageId, activate);
        } catch (IOException e) {
            logger.error("Failed uploading demo route Miami-Nuuk.txt", e);
        }
    }

}
