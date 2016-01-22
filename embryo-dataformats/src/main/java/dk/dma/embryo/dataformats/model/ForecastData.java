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

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dma.embryo.common.EmbryonicException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ForecastData {

    private String json;
    private ForecastMetaData metaData;
    private ForecastDataId id;

    public ForecastData(ForecastDataId id, String json) {
        this.id = id;
        this.json = json;
        this.metaData = new ForecastMetaData(id);
    }

    public String getJson() {
        return mergeWithMetaData();
    }

    private String mergeWithMetaData() {
        ObjectMapper mapper = new ObjectMapper();
        HashMap jsonMap;
        try {
            jsonMap = mapper.readValue(json, HashMap.class);
            ((Map)jsonMap.get("metadata")).putAll(metaData.asMap());
            return mapper.writeValueAsString(jsonMap);
        } catch (IOException e) {
            throw new EmbryonicException("Error merging forcast with meta data. Id:  " + getId(), e);
        }
    }

    public void add(ForecastMetaData additionalMetaData) {
        metaData = metaData.with(additionalMetaData);
    }

    public ForecastDataId getId() {
        return id;
    }
    public String getIdAsString() {
        return id.getId();
    }

    public ForecastHeader getHeader() {
        return ForecastHeader.createFrom(metaData);
    }
}
