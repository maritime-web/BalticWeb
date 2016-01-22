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
package dk.dma.embryo.sar;

import com.n1global.acc.CouchDbConfig;
import com.ning.http.client.AsyncHttpClient;
import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.user.model.SailorRole;
import dk.dma.embryo.user.model.SecuredUser;
import dk.dma.embryo.user.service.UserService;
import dk.dma.embryo.vessel.model.Vessel;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Jesper Tejlgaard on 11/12/15.
 */

@Singleton
@Startup
public class DatabaseInitializer {

    @Inject
    @Property(value = "embryo.couchDb.user")
    private String dbUser;

    @Inject
    @Property(value = "embryo.couchDb.password")
    private String dbPassword;

    @Inject
    @Property("embryo.couchDb.live.url")
    private String liveDbUrl;

    @Inject
    @Property("embryo.couchDb.user.url")
    private String userDbUrl;

    @Inject
    @Property("embryo.couchDb.user.cron")
    private ScheduleExpression userCron;

    @Resource
    private TimerService timerService;

    private final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    private UserDb userDb;

    @Inject
    private UserService userService;


    public void initializeUserDatabase() {
        if (userDbUrl != null && userDbUrl.trim().length() != 0) {
            logger.info("Initializing CouchDB with url {}", userDbUrl);

            AsyncHttpClient httpClient = new AsyncHttpClient();

            // automaticly creates database if not already

            CouchDbConfig.Builder builder = new CouchDbConfig.Builder().setHttpClient(httpClient)
                    .setServerUrl(userDbUrl).setDbName("embryo-user");

            if (dbUser != null && dbUser.trim().length() > 0 && dbPassword != null && dbPassword.trim().length() > 0) {
                builder.setUser(dbUser).setPassword(dbPassword);
            }

            userDb = new UserDb(builder.build());

            userDb.cleanupViews();

            if (userCron != null) {
                timerService.createCalendarTimer(userCron, new TimerConfig(null, false));
            }
        } else {
            logger.info("embryo.couchDb.user.url not set");
        }

    }


    @PostConstruct
    public void initialize() {

        if (liveDbUrl != null && liveDbUrl.trim().length() != 0) {
            logger.info("Initializing CouchDB with url {}", liveDbUrl);

            AsyncHttpClient httpClient = new AsyncHttpClient();

            // automaticly creates database if not already existing
            SarDb db = new SarDb(new CouchDbConfig.Builder().setHttpClient(httpClient)
                    .setServerUrl(liveDbUrl).setDbName("embryo-live")
                    .setUser(dbUser).setPassword(dbPassword)
                    .build());
            db.cleanupViews();
        } else {
            logger.info("embryo.couchDb.live.url not set");
        }


        initializeUserDatabase();


        /*
        try{
            CouchDbEventListener<SarDocument> listener = new CouchDbEventListener<SarDocument>(db, new CouchDbNotificationConfig.Builder().setIncludeDocs(true).build()) {
            //empty
            s};
            final CountDownLatch latch = new CountDownLatch(1);

            listener.addEventHandler(new CouchDbEventHandler<SarDocument>() {
                @Override
                public void onEvent(CouchDbEvent<SarDocument> event) {
                    System.out.println(event);
                    latch.countDown();
                }

                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                }
            });

            listener.startListening();

            latch.await();
        }catch(InterruptedException e){
            System.out.println(e);
        }
*/


    }

    private static String getUserName(SecuredUser user) {
        String name = user.getUserName();
        if (user.getRole().getClass() == SailorRole.class) {
            Vessel vessel = ((SailorRole) user.getRole()).getVessel();
            if (vessel.getAisData() != null && vessel.getAisData().getName() != null) {
                name = vessel.getAisData().getName();
            }
        }
        return name;
    }

    private static String getMmsi(SecuredUser user) {
        String mmsi = null;
        if (user.getRole().getClass() == SailorRole.class) {
            mmsi = ((SailorRole) user.getRole()).getVessel().getMmsi().toString();
        }
        return mmsi;
    }

    @Timeout
    public void replicateUsers() throws IOException {

        logger.info("replicating users from MySQL to CouchDB");
        List<SecuredUser> users = userService.list();

        List<User> usersView = userDb.getUsersView().<User>createDocQuery().asDocs();
        Map<String, User> couchUsers = User.toMap(usersView);

        List<User> newOrModifiedUsers = new ArrayList<>();

        // Add new users to couchdb
        for (SecuredUser user : users) {
            String id = user.getId().toString();
            if (!couchUsers.containsKey(id)) {
                String mmsi = getMmsi(user);
                String name = getUserName(user);
                newOrModifiedUsers.add(new User(id, name, mmsi));
                logger.info("Adding user with id={} and name={}", user.getId(), name);
            } else {
                User couchUser = couchUsers.get(id);
                String mmsi = getMmsi(user);
                String name = getUserName(user);
                if (!couchUser.getName().equals(name) || !ObjectUtils.equals(couchUser.getMmsi(), mmsi)) {
                    couchUser.setMmsi(mmsi.toString());
                    couchUser.setName(name);
                    logger.info("Updating user with id={} and name={}", id, name);
                    newOrModifiedUsers.add(couchUser);
                }
            }
        }
        userDb.bulk(newOrModifiedUsers);

        //remove deleted users from couchdb
        Map<Long, SecuredUser> securedUsers = users.stream().collect(Collectors.toMap(SecuredUser::getId, u -> u));
        List<User> toRemove = new ArrayList<>();
        for (User user : couchUsers.values()) {
            if (!securedUsers.containsKey(Long.parseLong(user.getDocId()))) {
                toRemove.add(user);
                user.setDeleted();
                logger.info("Removing user with id={} and name={}", user.getDocId(), user.getName());
            }
        }

        userDb.bulk(toRemove);
    }


}
