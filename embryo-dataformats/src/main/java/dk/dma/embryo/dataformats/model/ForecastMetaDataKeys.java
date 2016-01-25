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

/**
 * Created by Steen on 20-01-2016.
 */
public enum ForecastMetaDataKeys {
    ID("id"),
    TYPE("ftype"),
    AREA("area"),
    PROVIDER("provider"),
    JSON_SIZE("size"),
    TIMESTAMP("timestamp"),
    ORIGINAL_FILE_NAME("name");

    private final String keyName;

    ForecastMetaDataKeys(String keyName) {
        this.keyName = keyName;
    }

    public String getKeyName() {
        return keyName;
    }
}
