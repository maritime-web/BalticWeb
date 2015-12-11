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


    @JsView(map = "if (doc['@type'] && doc['@type'] === 'User') {" +
            "emit(doc.name.toLowerCase(), doc.mmsi);}", viewName = "userView", designName = "users")
    private CouchDbMapView<String, CouchDbDocument> userView;

    @JsView(map = "if (doc['@type'] && doc['@type'] === 'User') {" +
            "emit(doc.name.toLowerCase());}", viewName = "usersByNameView", designName = "users")
    private CouchDbMapView<String, CouchDbDocument> usersByNameView;

    @JsView(map = "if (doc['@type'] && doc['@type'] === 'User') {" +
            "emit(doc.mmsi);}", viewName = "usersByMmsiView", designName = "users")
    private CouchDbMapView<String, CouchDbDocument> usersByMmsiView;


    public CouchDbMapView<String, CouchDbDocument> getUserView() {
        return userView;
    }
}