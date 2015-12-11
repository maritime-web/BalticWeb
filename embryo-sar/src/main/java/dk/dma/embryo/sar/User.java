package dk.dma.embryo.sar;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.n1global.acc.json.CouchDbDocument;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * Simple Class name (i.e. 'User') is added as @type property on JSON document.
 *
 * @type name must be used in JavaScript code.
 */
@JsonTypeInfo(use = Id.NAME)
public class User extends CouchDbDocument {

    private String name;
    private Integer mmsi;

    public User(Long id, String name, Integer mmsi) {
        super(id.toString());
        this.name = name;
        this.mmsi = mmsi;
    }

    public String getName() {
        return name;
    }

    public Integer getMmsi() {
        return mmsi;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMmsi(Integer mmsi) {
        this.mmsi = mmsi;
    }
}
