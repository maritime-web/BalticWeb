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

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import dk.dma.embryo.common.persistence.BaseEntity;
import dk.dma.embryo.dataformats.model.ForecastType.Type;

/**
 * Model for a Forecast. The actual JSON data is found in the ForecastData
 * class, as it needs to be lazy loaded - we don't want to load that data when
 * we're just acquiring a list of forecasts.
 * 
 * @author avlund
 *
 */
@NamedQueries({
        @NamedQuery(name = "Forecast:lookup", query = "SELECT f FROM Forecast f WHERE f.name = :name AND f.ftype = :type"),
        @NamedQuery(name = "Forecast:list", query = "SELECT f FROM Forecast f WHERE f.ftype = :type AND f.size != -1 ORDER BY f.area, f.timestamp DESC"),
        @NamedQuery(name = "Forecast:exists", query = "SELECT COUNT(*) FROM Forecast f WHERE f.name = :name AND f.timestamp = :timestamp"),
        @NamedQuery(name = "Forecast:findDuplicates", query = "SELECT f FROM Forecast f WHERE f.area = :area AND f.provider = :provider AND f.ftype = :type ORDER by f.timestamp DESC, f.size DESC"),
        @NamedQuery(name = "Forecast:exactlySame", query = "SELECT f FROM Forecast f WHERE f.name = :name AND f.ftype = :type AND f.area = :area") })
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

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name="data_id"))
    private ForecastDataId forecastDataId;

    @Enumerated(EnumType.STRING)
    private Type ftype;

    @SuppressWarnings("unused")
    protected Forecast() {
    }

    public Forecast(String name, ForecastDataId forecastDataId, Type type, int size, Provider provider, long timestamp, String area) {
        this.name = name;
        this.ftype = type;
        this.forecastDataId = forecastDataId;
        this.size = size;
        this.provider = provider;
        this.timestamp = timestamp;
        this.area = area;
    }

    public String getName() {
        return name;
    }

    public ForecastDataId getForecastDataId() {
        return forecastDataId;
    }

    public void updateData(ForecastDataId forecastDataId, int size) {
        this.forecastDataId = forecastDataId;
        this.size = size;
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

    public void invalidate() {
        forecastDataId = null;
        size = -1;
    }

    public static enum Provider {
        DMI, FCOO
    }

    @Override
    public String toString() {
        return "Forecast: provider: " + provider + ", type: " + ftype + ", area: " + area + ", size: " + size + ", time stamp: " + timestamp + ", name: "
                + name;
    }
}
