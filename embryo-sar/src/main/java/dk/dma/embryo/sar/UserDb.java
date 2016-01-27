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

import com.n1global.acc.CouchDb;
import com.n1global.acc.CouchDbConfig;
import com.n1global.acc.annotation.JsView;
import com.n1global.acc.view.CouchDbMapView;

public class UserDb extends CouchDb {
    public UserDb(CouchDbConfig config) {
        super(config);
    }


    /**
     * Used server side to update embryo-user Couch database.
     * It is apparently necessary to emit doc._id for Jackson deserialization to work.
     */
    @JsView(map = "if (doc['@class'] && doc['@class'] == 'dk.dma.embryo.sar.User') {" +
            "emit(doc._id);}", viewName = "usersView", designName = "users")
    private CouchDbMapView<String, User> usersView;

    @JsView(map = "if (doc['@class'] && doc['@class'] == 'dk.dma.embryo.sar.User') {" +
            "emit(doc.name.toLowerCase(), doc.mmsi);}", viewName = "userView", designName = "users")
    private CouchDbMapView<String, User> userView;

    @JsView(map = "if (doc['@class'] && doc['@class'] == 'dk.dma.embryo.sar.User') {" +
            "emit(doc.name.toLowerCase());}", viewName = "usersByNameView", designName = "users")
    private CouchDbMapView<String, User> usersByNameView;

    @JsView(map = "if (doc['@class'] && doc['@class'] == 'dk.dma.embryo.sar.User') {" +
            "emit(doc.mmsi);}", viewName = "usersByMmsiView", designName = "users")
    private CouchDbMapView<String, User> usersByMmsiView;


    public CouchDbMapView<String, User> getUsersView() {
        return usersView;
    }
}
