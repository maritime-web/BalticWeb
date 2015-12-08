package dk.dma.embryo.sar;

import com.n1global.acc.CouchDbConfig;
import com.ning.http.client.AsyncHttpClient;
import dk.dma.embryo.common.configuration.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

/**
 * Created by Jesper Tejlgaard on 11/12/15.
 */

@Singleton
@Startup
public class DatabaseInitializer {

    @Inject
    @Property("embryo.live.db.url")
    private String liveDbUrl;

    @Inject
    @Property("embryo.live.db.user")
    private String liveDbUser;

    @Inject
    @Property("embryo.live.db.password")
    private String liveDbPassword;

    private final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    @PostConstruct
    public void initialize() {

        if (liveDbUrl != null && liveDbUrl.trim().length() != 0) {
            logger.info("Initializing CouchDB with url {}", liveDbUrl);

            AsyncHttpClient httpClient = new AsyncHttpClient();

            // automaticly creates database if not already existing
            SarDb db = new SarDb(new CouchDbConfig.Builder().setHttpClient(httpClient)
                    .setServerUrl(liveDbUrl).setDbName("embryo-live")
                    .setUser(liveDbUser).setPassword(liveDbPassword)
                    .build());
            db.cleanupViews();
        } else {
            logger.info("embryo.live.db.url not set");
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


}
