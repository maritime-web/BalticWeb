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
package dk.dma.embryo.vessel.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dma.embryo.common.configuration.Property;
import org.jboss.resteasy.client.ProxyFactory;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

@Singleton
public class AisTrackClientFactory {

    @Inject
    @Property("dk.dma.embryo.restclients.fullAisViewServiceInclNorwegianDataUrl")
    private String fullAisViewServiceInclNorwegianDataUrl;
    
    @Inject
    @Property("dk.dma.embryo.restclients.aisRestBaseUrl")
    private String aisRestBaseUrl;

    @Inject
    @Property("embryo.ais.server.url")
    private String aisServerUrl;


    @Produces
    public AisViewServiceAllAisData createFullAisViewInclNorwegianDataService() {
        return ProxyFactory.create(AisViewServiceAllAisData.class, fullAisViewServiceInclNorwegianDataUrl);
    }
    
    @Produces
    public AisTrackClient createAisRestDataService() {
        return ProxyFactory.create(AisTrackClient.class, aisServerUrl);
    }


    public static String asJson(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
