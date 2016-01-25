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

import dk.dma.embryo.common.configuration.Property;

import javax.enterprise.inject.Produces;

/**
 * Created by Steen on 18-01-2016.
 *
 */
public class CouchClientFactory {
    @Produces
    @CouchDatabase(databaseName = "forecast")
    public HttpCouchClient createHttpCouchClient(@Property("embryo.couchDb.forecast.url")String forecastDbUrl,
                                                 @Property("embryo.couchDb.forecast.design.resource.url")String designDocumentResourceUrl,
                                                 @Property("embryo.couchDb.forecast.design.document.id")String designDocumentId) {
        CouchDbConfig config = new CouchDbConfig(forecastDbUrl, designDocumentResourceUrl, designDocumentId);
        return new HttpCouchClient(config);
    }

}
