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
import com.n1global.acc.json.CouchDbDocument;
import com.n1global.acc.view.CouchDbMapView;

public class SarDb extends CouchDb {
    public SarDb(CouchDbConfig config) {
        super(config);
    }


    /*

            var ddoc = {
            _id: '_design/sareffortview',
            views: {
                sareffortview: {
                    map: function (doc) {
                        if (doc.docType === embryo.sar.Type.EffortAllocation || doc.docType === embryo.sar.Type.SearchPattern) {
                            emit(doc.sarId);
                        }
                    }.toString()
                }
            }
        }

     */
    // TODO modify async-couchdb-client such that javascript attribute is not added to design documents.
    @JsView(map = "if (doc['@type'] === 'Allocation' || doc['@type'] === 'Pattern') {" +
            "emit(doc.sarId);}", viewName = "effortView", designName = "sar")
    private CouchDbMapView<String, CouchDbDocument> effortView;

    /*
        // create a design doc
        var ddoc = {
            _id: '_design/sarlogview',
            views: {
                sarlogview: {
                    map: function (doc) {
                        if (doc.msgSarId) {
                            emit(doc.msgSarId);
                        }
                    }.toString()
                }
            }
        }
     */
    // TODO modify async-couchdb-client such that javascript attribute is not added to design documents.
    @JsView(designName = "sar", viewName = "logView", map = "if (doc['@type'] === 'SarLog') {emit(doc.sarId);}")
    private CouchDbMapView<String, CouchDbDocument> logView;

    /*
            var ddoc2 = {
            _id: '_design/sarsearchpattern',
            views: {
                sarsearchpattern: {
                    map: function (doc) {
                        if (doc.$type === embryo.sar.Type.SearchPattern) {
                            emit(doc.sarId);
                        }
                    }.toString()
                }
            }
        }

     */

    // TODO modify async-couchdb-client such that javascript attribute is not added to design documents.
    @JsView(designName = "sar", viewName = "searchPattern", map = "if (doc['@type'] === 'Pattern') {emit(doc.sarId);}")
    private CouchDbMapView<String, CouchDbDocument> searchPatternView;
}
