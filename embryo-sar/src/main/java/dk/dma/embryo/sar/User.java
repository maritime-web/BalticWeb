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
