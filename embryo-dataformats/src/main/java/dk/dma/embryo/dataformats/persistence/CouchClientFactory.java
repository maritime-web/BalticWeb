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
@SuppressWarnings("unused")
public class CouchClientFactory {
    @Produces
    @CouchDatabase(databaseName = "forecast")
    public HttpCouchClient createHttpCouchClient(@Property("embryo.couchDb.forecast.db")String forecastDb,
                                                 @Property("embryo.couchDb.host")String host,
                                                 @Property("embryo.couchDb.port")int port,
                                                 @Property("embryo.couchDb.user")String user,
                                                 @Property("embryo.couchDb.password")String password,
                                                 @Property("embryo.couchDb.forecast.design.resource.url")String designDocumentResourceUrl,
                                                 @Property("embryo.couchDb.forecast.design.document.id")String designDocumentId) {
        CouchDbConfig config = new CouchDbConfig(forecastDb, designDocumentResourceUrl, designDocumentId, host, port, user, password);
        return new HttpCouchClient(config);
    }

}
