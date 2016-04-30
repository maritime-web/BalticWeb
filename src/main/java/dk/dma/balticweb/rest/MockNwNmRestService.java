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
package dk.dma.balticweb.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.niord.model.vo.MessageVo;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dummy service that fetches NW-NM messages.
 */
@Singleton
@Startup
@Path("/nw-nm")
public class MockNwNmRestService {

    @Inject
    private Logger logger;

    private Map<String, String> cachedServiceInstanceUrls = new HashMap<>();


    /**
     * Sets up dummy service-urls that should really be resolved via the MC Service Registry
     */
    @PostConstruct
    void init() {
        cachedServiceInstanceUrls.put(
                "urn:mrnx:mcl:service:instance:dma:nw-nm:v0.1",
                "http://niord.e-navigation.net/rest/public/v1/messages"
        );
    }


    /**
     * Fetches the published messages from the service associated with the given MC Service Registry
     * @param instanceId the MC Service Registry instance ID of the NW-NM service
     * @param lang the ISO-2 language code, e.g. "en"
     * @param wkt optionally a WKT for the geometry extent of the area
     */
    @GET
    @Path("/messages")
    @Produces("application/json;charset=UTF-8")
    @GZIP
    @NoCache
    public List<MessageVo> getNwNmMessages(
            @QueryParam("instanceId") String instanceId,
            @QueryParam("lang") @DefaultValue("en") String lang,
            @QueryParam("wkt") String wkt)  {

        if (StringUtils.isBlank(instanceId) || !cachedServiceInstanceUrls.containsKey(instanceId)) {
            throw new WebApplicationException("Invalid NW-NM MC Service Registry instance ID: " + instanceId, 403);
        }

        // Create the URL
        String url = cachedServiceInstanceUrls.get(instanceId);

        StringBuilder params = new StringBuilder();
        checkConcatParam(params, "lang", lang);
        checkConcatParam(params, "wkt", wkt);
        url += params;

        long t0 = System.currentTimeMillis();
        try {
            URLConnection con = new URL(url).openConnection();
            con.setConnectTimeout(5000); //  5 seconds
            con.setReadTimeout(10000);   // 10 seconds

            try (InputStream is = con.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                List<MessageVo> messages = mapper.readValue(is, new TypeReference<List<MessageVo>>(){});

                logger.info(String.format(
                        "Loaded %d NW-NM messages in %s ms",
                        messages.size(),
                        System.currentTimeMillis() - t0));

                return messages;
            }

        } catch (Exception e) {
            logger.error("Failed loading NW-NM messages: " + e.getMessage());
            throw new WebApplicationException("Failed loading NW-NM messages: " + e.getMessage(), 500);
        }
    }

    /** If defined, appends the given parameter */
    private StringBuilder checkConcatParam(StringBuilder params, String name, String value) {
        if (StringUtils.isNotBlank(value)) {
            try {
                params.append(params.length() == 0 ? "?" : "&");
                params.append(name).append("=").append(URLEncoder.encode(value, "UTF-8"));
            } catch (UnsupportedEncodingException ignored) {
            }
        }
        return params;
    }
}
