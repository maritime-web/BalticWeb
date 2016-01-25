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

import java.util.Objects;

/**
 * Value class representing identification of a forecast data document.
 */
public class ForecastDataId {
    private String area;

    private ForecastProvider provider;

    private Type type;

    private String id;

    public ForecastDataId(String area, ForecastProvider provider, Type type) {
        if (area == null) {
            throw new NullPointerException();
        }
        if (provider == null) {
            throw new NullPointerException();
        }
        if (type == null) {
            throw new NullPointerException();
        }

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

    public ForecastProvider getProvider() {
        return provider;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ForecastDataId that = (ForecastDataId) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ForecastDataId{" +
                "area='" + area + '\'' +
                ", provider=" + provider +
                ", type=" + type +
                ", id='" + id + '\'' +
                '}';
    }
}
