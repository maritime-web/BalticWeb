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
package dk.dma.embryo.common.firebase;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * Created by Jesper Tejlgaard on 2/20/15.
 */
@Singleton
@Startup
public class FirebaseListener {

    @PostConstruct
    public void startup() throws Exception {
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
    }

}
