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

package dk.dma.embryo.dataformats.persistence;

/**
 * Created by Steen on 12-01-2016.
 *
 */
public class CouchDbConfig {
    private final String forecastDbUrl;
    private final String designDocumentResourceUrl;
    private final String designDocumentId;

    public CouchDbConfig(String forecastDbUrl, String designDocumentResourceUrl, String designDocumentId) {
        this.forecastDbUrl = forecastDbUrl;
        this.designDocumentResourceUrl = designDocumentResourceUrl;
        this.designDocumentId = designDocumentId;
    }

    public String getForecastDbUrl() {
        return forecastDbUrl;
    }

    public String getDesignDocumentResourceUrl() {
        return designDocumentResourceUrl;
    }

    public String getDesignDocumentId() {
        return designDocumentId;
    }
}
