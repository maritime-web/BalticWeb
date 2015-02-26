package dk.dma.embryo.common.firebase;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import org.glassfish.jersey.media.sse.EventListener;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.net.URI;

/**
 * Created by Jesper Tejlgaard on 2/20/15.
 */
public class FirebaseApplication {

    @Test
    public void test() throws Exception {

        Client client = ClientBuilder.newBuilder()
                .register(SseFeature.class).build();
        WebTarget webTarget = client.target(new URI("http://incandescent-torch-4183.firebaseio.com/logs.json"));
        EventSource eventSource = new EventSource(webTarget) {
            @Override
            public void onEvent(InboundEvent inboundEvent) {
                System.out.println("Data " + inboundEvent.readData());
            }
        };

        Thread.sleep(20000);

        System.out.println("Exit");

        eventSource.close();


    }

    @Test
    public void test2() throws Exception {

        Client client = ClientBuilder.newBuilder()
                .register(SseFeature.class).build();
        WebTarget target = client.target("http://incandescent-torch-4183.firebaseio.com/logs.json");
        EventSource eventSource = EventSource.target(target).build();
        EventListener listener = new EventListener() {
            @Override
            public void onEvent(InboundEvent inboundEvent) {
                System.out.println(inboundEvent.getName() + "; "
                        + inboundEvent.readData(String.class));
            }
        };
        eventSource.register(listener, "message-to-client");
        eventSource.open();
        System.out.println("open");


        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(20000);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        t.start();

        t.join();

        System.out.println("exit");
    }

    @Test
    public void test3() throws Exception {
        Firebase.setDefaultConfig(Firebase.getDefaultConfig());

        Firebase myFirebaseRef = new Firebase("http://incandescent-torch-4183.firebaseio.com/");

/*        myFirebaseRef.child("logs").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println(snapshot.getValue());  //prints "Do you have data? You'll love Firebase."
            }
            @Override public void onCancelled(FirebaseError error) {
                System.out.println(error.getMessage());  //prints "Do you have data? You'll love Firebase."
            }
        });
*/
        myFirebaseRef.child("messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String s) {
                System.out.println("added: " + s + " " + snapshot.getValue());  //prints "Do you have data? You'll love Firebase."
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
                System.out.println("removed: " + snapshot.getValue());  //prints "Do you have data? You'll love Firebase."

            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String s) {
                System.out.println("changed: " + s + " " + snapshot.getValue());  //prints "Do you have data? You'll love Firebase."

            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String s) {
                System.out.println("moved: " + s + " " + snapshot.getValue());  //prints "Do you have data? You'll love Firebase."

            }

            @Override
            public void onCancelled(FirebaseError var1) {
                System.out.println("cancelled");
            }
        });


        Thread.sleep(20000);
    }

}
