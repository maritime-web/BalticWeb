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
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.specimpl.ResteasyUriBuilder;
import org.slf4j.Logger;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

@Singleton
public class AisClientFactory {

    @Inject
    Logger logger;

    @Inject
    @Property("embryo.aistrack.server.url")
    private String aisTrackUrl;

    @Inject
    @Property("embryo.aistrack.server.user")
    private String aisTrackUser;

    @Inject
    @Property("embryo.aistrack.server.pwd")
    private String aisTrackPwd;


    @Inject
    @Property("embryo.aisstore.server.url")
    private String aisStoreUrl;

    @Inject
    @Property("embryo.aisstore.server.user")
    private String aisStoreUser;

    @Inject
    @Property("embryo.aisstore.server.pwd")
    private String aisStorePwd;


    @Produces
    public AisTrackClient createAisTrackClient() {
        return createClient(AisTrackClient.class, aisTrackUrl, aisTrackUser, aisTrackPwd);
    }

    @Produces
    public AisStoreClient createAisStoreClient() {
        return createClient(AisStoreClient.class, aisStoreUrl, aisStoreUser, aisStorePwd);
    }

    private static <T> T createClient(Class<T> type, String url, String user, String pwd) {
        ResteasyClientBuilder clientBuilder = new ResteasyClientBuilder();
        if (enableHttpBasic(user, pwd)) {
            clientBuilder.register(new Authenticator(user, pwd));
        }
        ResteasyWebTarget target = clientBuilder.build().target(ResteasyUriBuilder.fromUri(url));
        return target.proxy(type);
    }

    private static boolean enableHttpBasic(String user, String pwd) {
        return user != null && user.trim().length() > 0 && pwd != null && pwd.trim().length() > 0;
    }

    public static String asJson(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Authenticator support Basic Http Authentication
     * <p/>
     * Borrowed from Adam Bien: http://www.adam-bien.com/roller/abien/entry/client_side_http_basic_access
     */

    public static class Authenticator implements ClientRequestFilter {

        private final String user;
        private final String password;

        public Authenticator(String user, String password) {
            this.user = user;
            this.password = password;
        }

        public void filter(ClientRequestContext requestContext) throws IOException {
            MultivaluedMap<String, Object> headers = requestContext.getHeaders();
            final String basicAuthentication = getBasicAuthentication();
            headers.add("Authorization", basicAuthentication);

        }

        private String getBasicAuthentication() {
            String token = this.user + ":" + this.password;
            try {
                return "BASIC " + DatatypeConverter.printBase64Binary(token.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                throw new IllegalStateException("Cannot encode with UTF-8", ex);
            }
        }
    }
}
