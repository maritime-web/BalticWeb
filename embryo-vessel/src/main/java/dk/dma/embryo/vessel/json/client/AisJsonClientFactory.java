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
package dk.dma.embryo.vessel.json.client;

import java.io.IOException;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.resteasy.client.ProxyFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import dk.dma.embryo.common.configuration.Property;

@Singleton
public class AisJsonClientFactory {
    @Inject
    @Property("dk.dma.embryo.restclients.dmiSejlRuteServiceUrl")
    private String dmiSejlRuteServiceUrl;

    @Inject
    @Property("dk.dma.embryo.restclients.fullAisViewServiceInclNorwegianDataUrl")
    private String fullAisViewServiceInclNorwegianDataUrl;
    
    @Inject
    @Property("dk.dma.embryo.restclients.aisRestBaseUrl")
    private String aisRestBaseUrl;
    
    @Produces
    public AisViewServiceAllAisData createFullAisViewInclNorwegianDataService() {
        return ProxyFactory.create(AisViewServiceAllAisData.class, fullAisViewServiceInclNorwegianDataUrl);
    }
    
    @Produces
    public AisRestDataService createAisRestBaseUrlDataService() {
        return ProxyFactory.create(AisRestDataService.class, aisRestBaseUrl);
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
