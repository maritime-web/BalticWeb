package dk.dma.embryo.sar;

import com.n1global.acc.CouchDbConfig;
import com.ning.http.client.AsyncHttpClient;
import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.user.model.SailorRole;
import dk.dma.embryo.user.model.SecuredUser;
import dk.dma.embryo.user.service.UserService;
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
import java.util.stream.Stream;

/**
 * Created by Jesper Tejlgaard on 11/12/15.
 */

@Singleton
@Startup
public class DatabaseInitializer {

    @Inject
    @Property("embryo.couchDb.user")
    private String dbUser;

    @Inject
    @Property("embryo.couchDb.password")
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

        if (userDbUrl != null && userDbUrl.trim().length() != 0) {
            logger.info("Initializing CouchDB with url {}", userDbUrl);

            AsyncHttpClient httpClient = new AsyncHttpClient();

            // automaticly creates database if not already existing
            userDb = new UserDb(new CouchDbConfig.Builder().setHttpClient(httpClient)
                    .setServerUrl(userDbUrl).setDbName("embryo-user")
                    .setUser(dbUser).setPassword(dbPassword)
                    .build());
            userDb.cleanupViews();

            timerService.createCalendarTimer(userCron, new TimerConfig(null, false));
        } else {
            logger.info("embryo.couchDb.user.url not set");
        }



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

    @Timeout
    public void timeout() throws IOException {

        logger.info("replicating users from MySQL to CouchDB");

        List<SecuredUser> users = userService.list();

        Stream<User> userStream = userDb.getUserView().<User>createDocQuery().asDocs().stream();
        //Stream<User> userStream = userDb.getBuiltInView().<User>createDocQuery().asDocs().stream();
        Map<String, User> couchUsers = userStream.filter(d -> d.getClass() == User.class).collect(Collectors.toMap(User::getDocId, user -> user));//filter design docs if exists

        List<User> newUsers = new ArrayList<>();

        // Add new users to couchdb
        for (SecuredUser user : users) {
            if (!couchUsers.containsKey(user.getId())) {
                Integer mmsi = null;
                if (user.getRole().getClass() == SailorRole.class) {
                    mmsi = ((SailorRole) user.getRole()).getVessel().getMmsi().intValue();
                }
                newUsers.add(new User(user.getId(), user.getUserName(), mmsi));
            }
        }
        userDb.bulk(newUsers);

        //remove deleted users from couchdb
        Map<Long, SecuredUser> securedUsers = users.stream().collect(Collectors.toMap(SecuredUser::getId, u -> u));
        List<User> toRemove = new ArrayList<>();
        for (User user : couchUsers.values()) {
            if (!securedUsers.containsKey(user.getDocId())) {
                toRemove.add(user);
                user.setDeleted();
            }
        }

        userDb.bulk(toRemove);
    }


}
