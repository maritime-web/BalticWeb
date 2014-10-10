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
package dk.dma.embryo.dataformats.model;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import dk.dma.embryo.common.persistence.BaseEntity;
import dk.dma.embryo.dataformats.model.ForecastType.Type;

@NamedQueries({
        @NamedQuery(name = "Forecast:lookup", query = "SELECT f FROM Forecast f WHERE f.name = :name AND f.ftype = :type"),
        @NamedQuery(name = "Forecast:find", query = "SELECT f FROM Forecast f JOIN FETCH f.data WHERE f.id = :id"),
        @NamedQuery(name = "Forecast:list", query = "SELECT f FROM Forecast f WHERE f.ftype = :type AND f.size != -1 ORDER BY f.timestamp DESC"),
        @NamedQuery(name = "Forecast:exists", query = "SELECT COUNT(*) FROM Forecast f WHERE f.name = :name"),
        @NamedQuery(name = "Forecast:findDuplicate", query = "SELECT f FROM Forecast f WHERE f.area = :area AND f.provider = :provider AND f.timestamp = :timestamp AND f.ftype = :type") })
@Entity
@JsonIgnoreProperties({ "data" })
public class Forecast extends BaseEntity<Long> {

    private static final long serialVersionUID = 4876253028801534394L;

    private String name;

    @Enumerated(EnumType.STRING)
    private Provider provider;

    private long timestamp;

    private int size;

    private String area;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn
    @Basic(fetch = FetchType.LAZY)
    private ForecastData data;

    @Enumerated(EnumType.STRING)
    private Type ftype;

    @SuppressWarnings("unused")
    private Forecast() {
    }

    public Forecast(String name, String json, Type type, int size, Provider provider, long timestamp, String area) {
        this.name = name;
        this.ftype = type;
        this.data = new ForecastData(json);
        this.size = size;
        this.provider = provider;
        this.timestamp = timestamp;
        this.area = area;
    }

    public String getName() {
        return name;
    }

    public String getData() {
        if (data != null) {
            return data.getJson();
        }
        return null;
    }

    public Type getType() {
        return ftype;
    }

    public Provider getProvider() {
        return provider;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getSize() {
        return size;
    }

    public String getArea() {
        return area;
    }

    public static enum Provider {
        DMI, FCOO
    }
}
