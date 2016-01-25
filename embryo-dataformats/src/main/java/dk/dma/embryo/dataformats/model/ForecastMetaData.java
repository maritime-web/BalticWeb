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

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static dk.dma.embryo.dataformats.model.ForecastMetaDataKeys.AREA;
import static dk.dma.embryo.dataformats.model.ForecastMetaDataKeys.ID;
import static dk.dma.embryo.dataformats.model.ForecastMetaDataKeys.JSON_SIZE;
import static dk.dma.embryo.dataformats.model.ForecastMetaDataKeys.ORIGINAL_FILE_NAME;
import static dk.dma.embryo.dataformats.model.ForecastMetaDataKeys.PROVIDER;
import static dk.dma.embryo.dataformats.model.ForecastMetaDataKeys.TIMESTAMP;
import static dk.dma.embryo.dataformats.model.ForecastMetaDataKeys.TYPE;

/**
 * Created by Steen on 18-01-2016.
 *
 */
public class ForecastMetaData {
    private HashMap<String, Object> metaData;

    public ForecastMetaData(ForecastDataId id) {
        this();
        metaData.put(ID.getKeyName(), id.getId());
        metaData.put(TYPE.getKeyName(), id.getType().name());
        metaData.put(AREA.getKeyName(), id.getArea());
        metaData.put(PROVIDER.getKeyName(), id.getProvider().name());
    }

    public ForecastMetaData() {
        this.metaData = new HashMap<>();
    }

    private ForecastMetaData(HashMap<String, Object> metaData) {
        this.metaData = metaData;
    }

    public ForecastMetaData withJsonSize(int jsonSize) {
        if (jsonSize <= 0) {
            throw new IllegalArgumentException("Json size must be greater than zero");
        }

        return with(createWith(JSON_SIZE.getKeyName(), jsonSize));
    }

    public ForecastMetaData withTimestamp(long timestamp) {
        if (timestamp <= 0) {
            throw new IllegalArgumentException("Timestamp must be greater than zero");
        }

        return with(createWith(TIMESTAMP.getKeyName(), timestamp));
    }

    public ForecastMetaData withOriginalFileName(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("File name must contain at least one character");
        }

        return with(createWith(ORIGINAL_FILE_NAME.getKeyName(), fileName));
    }

    public ForecastMetaData with(ForecastMetaData additionalMetaData) {
        HashMap<String, Object> mergedMetaData = new HashMap<>(metaData);
        mergedMetaData.putAll(additionalMetaData.metaData);
        return new ForecastMetaData(mergedMetaData);
    }

    public Map<?, ?> asMap() {
        HashMap<String, Object> theData = new HashMap<>();
        HashMap<String, Object> headerData = new HashMap<>();
        headerData.putAll(metaData);
        theData.put("header", headerData);

        return theData;
    }

    private ForecastMetaData createWith(String key, Object value) {
        HashMap<String, Object> metaData = new HashMap<>();
        metaData.put(key, value);
        return new ForecastMetaData(metaData);
    }
}
