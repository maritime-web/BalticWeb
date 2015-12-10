package dk.dma.embryo.sar;

import com.n1global.acc.CouchDb;
import com.n1global.acc.CouchDbConfig;
import com.n1global.acc.annotation.JsView;
import com.n1global.acc.json.CouchDbDocument;
import com.n1global.acc.view.CouchDbMapView;

public class UserDb extends CouchDb {
    public UserDb(CouchDbConfig config) {
        super(config);
    }


    @JsView(map = "if (doc['@type'] === 'User') {" +
            "emit(doc.name.toLowerCase(), doc.mmsi);}", viewName = "userView", designName = "users")
    private CouchDbMapView<String, CouchDbDocument> userView;

    public CouchDbMapView<String, CouchDbDocument> getUserView() {
        return userView;
    }

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
}