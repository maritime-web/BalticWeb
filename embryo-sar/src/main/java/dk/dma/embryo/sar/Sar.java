package dk.dma.embryo.sar;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.n1global.acc.json.CouchDbDocument;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * Simple Class name (i.e. 'Sar') is added as @type property on JSON document.
 *
 * @type name must be used in JavaScript code.
 */
@JsonTypeInfo(use = Id.NAME)
public class Sar extends CouchDbDocument {

}
