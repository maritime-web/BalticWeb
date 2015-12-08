package dk.dma.embryo.sar;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.n1global.acc.json.CouchDbDocument;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * Class is used as @type attribute on JSON document.
 *
 * @type name must be used in JavaScript code.
 * Class name and value of JavaScript variable embryo.sar.Type.SearchArea
 */
@JsonTypeInfo(use = Id.NAME, property = "$type")
public class Sar extends CouchDbDocument {

}
