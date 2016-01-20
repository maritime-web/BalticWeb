/*
 *  Copyright (c) 2011 Danish Maritime Authority.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package dk.dma.embryo.dataformats.model;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

/**
 * Value class representing identification of a forecast data document.
 */
@Embeddable
public class ForecastDataId {
    @Transient
    private String area;

    @Transient
    private Forecast.Provider provider;

    @Transient
    private ForecastType.Type type;

    @Access(AccessType.FIELD)
    private String id;

    public ForecastDataId(String area, Forecast.Provider provider, ForecastType.Type type) {
        if (area == null) throw new NullPointerException();
        if (provider == null) throw new NullPointerException();
        if (type == null) throw new NullPointerException();

        this.area = area;
        this.provider = provider;
        this.type = type;
        this.id = (area + provider + type).replaceAll("\\s", "-");
    }

    //Required by JPA
    protected ForecastDataId() {
    }

    public String getId() {
        return id;
    }

    public String getArea() {
        return area;
    }

    public Forecast.Provider getProvider() {
        return provider;
    }

    public ForecastType.Type getType() {
        return type;
    }
}
