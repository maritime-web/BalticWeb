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
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.slf4j.Logger;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

@Singleton
public class AisTrackClientFactory {

    @Inject
    Logger logger;

    @Inject
    @Property("dk.dma.embryo.restclients.fullAisViewServiceInclNorwegianDataUrl")
    private String fullAisViewServiceInclNorwegianDataUrl;
    
    @Inject
    @Property("dk.dma.embryo.restclients.aisRestBaseUrl")
    private String aisRestBaseUrl;

    @Inject
    @Property("embryo.aistrack.server.url")
    private String aisTrackUrl;

    @Inject
    @Property("embryo.aistrack.server.user")
    private String aisTrackUser;

    @Inject
    @Property("embryo.aistrack.server.pwd")
    private String aisTrackPwd;


    @Produces
    public AisViewServiceAllAisData createFullAisViewInclNorwegianDataService() {
        return ProxyFactory.create(AisViewServiceAllAisData.class, fullAisViewServiceInclNorwegianDataUrl);
    }
    
    @Produces
    public AisTrackClient createAisTrackClient() {
        if (enableHttpBasic()) {
            logger.debug("Creating {} with HTTP Basic authentication for user {}", AisTrackClient.class.getSimpleName(), aisTrackUser);
            DefaultHttpClient httpClient = new DefaultHttpClient();
            Credentials credentials = new UsernamePasswordCredentials(aisTrackUser, aisTrackPwd);
            httpClient.getCredentialsProvider().setCredentials(org.apache.http.auth.AuthScope.ANY, credentials);
            ClientExecutor clientExecutor = new ApacheHttpClient4Executor(httpClient);
            return ProxyFactory.create(AisTrackClient.class, aisTrackUrl, clientExecutor);
        }

        return ProxyFactory.create(AisTrackClient.class, aisTrackUrl);
    }

    boolean enableHttpBasic() {
        return aisTrackUser != null && aisTrackUser.trim().length() > 0 && aisTrackPwd != null && aisTrackPwd.trim().length() > 0;
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
