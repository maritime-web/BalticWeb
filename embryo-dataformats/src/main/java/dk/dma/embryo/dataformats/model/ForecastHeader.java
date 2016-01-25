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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.Optional;

/**
 * Created by Steen on 21-01-2016.
 *
 */
public class ForecastHeader {
    private Optional<String> id;

    private Optional<String> name;

    private Optional<ForecastProvider> provider;

    private long timestamp;

    private int size;

    private Optional<String> area;

    private Optional<Type> ftype;

    ////////////////////////////////////////////////////
    // constructor
    ///////////////////////////////////////////////////
    public ForecastHeader() {
        this.id = Optional.empty();
        this.name = Optional.empty();
        this.provider = Optional.empty();
        this.area = Optional.empty();
        this.ftype = Optional.empty();
    }

    ////////////////////////////////////////////////////
    // business logic
    ///////////////////////////////////////////////////
    public boolean isBetterThan(ForecastHeader other) {
        return hasSameIdAs(other) && (isNewerThan(other) || (isFromSameFileAs(other) && isBiggerThan(other)));
    }

    private boolean hasSameIdAs(ForecastHeader other) {
        return getForecastDataId().equals(other.getForecastDataId());
    }

    private Optional<ForecastDataId> getForecastDataId() {
        if (area.isPresent() && provider.isPresent() && ftype.isPresent()) {
            return Optional.of(new ForecastDataId(area.get(), provider.get(), ftype.get()));
        }
        return  Optional.empty();
    }

    private boolean isNewerThan(ForecastHeader other) {
        return timestamp > other.timestamp;
    }

    private boolean isFromSameFileAs(ForecastHeader other) {
        return name.equals(other.name);
    }

    private boolean isBiggerThan(ForecastHeader other) {
        return size > other.size;
    }

    public static ForecastHeader createFrom(ForecastDataId id) {
        return createFrom(new ForecastMetaData(id));
    }

    public static ForecastHeader createFrom(ForecastMetaData metaData) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return  mapper.convertValue(metaData.asMap().get("header"), ForecastHeader.class);
    }

    public static ForecastHeader createFrom(String json) {
        if (StringUtils.isBlank(json)) {
            return new ForecastHeader();
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            return mapper.readValue(json, ForecastHeader.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not deserialize \""+json+"\" into ForecastHeader", e);
        }
    }
    ////////////////////////////////////////////////////
    // Object methods
    ///////////////////////////////////////////////////
    @Override
    public String toString() {
        return "ForecastHeader{" +
                "id=" + id +
                ", name=" + name +
                ", provider=" + provider +
                ", timestamp=" + timestamp +
                ", size=" + size +
                ", area=" + area +
                ", ftype=" + ftype +
                '}';
    }


    ////////////////////////////////////////////////////
    // Property methods
    ///////////////////////////////////////////////////
    public String getId() {
        return id.orElse(null);
    }

    private void setId(String id) {
        this.id = Optional.ofNullable(id);
    }

    public String getName() {
        return name.orElse(null);
    }

    private void setName(String name) {
        this.name = Optional.ofNullable(name);
    }

    public ForecastProvider getProvider() {
        return provider.orElse(null);
    }

    private void setProvider(ForecastProvider provider) {
        this.provider = Optional.ofNullable(provider);
    }

    public long getTimestamp() {
        return timestamp;
    }

    private void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getSize() {
        return size;
    }

    private void setSize(int size) {
        this.size = size;
    }

    public String getArea() {
        return area.orElse(null);
    }

    private void setArea(String area) {
        this.area = Optional.ofNullable(area);
    }

    public Type getFtype() {
        return ftype.orElse(null);
    }

    private void setFtype(Type ftype) {
        this.ftype = Optional.ofNullable(ftype);
    }
}
