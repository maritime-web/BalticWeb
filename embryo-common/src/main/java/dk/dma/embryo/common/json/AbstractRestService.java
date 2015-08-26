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
package dk.dma.embryo.common.json;

import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

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
/**
 * The Class AbstractRestService.
 *
 * @author ThomasBerg
 */
public abstract class AbstractRestService {
    
    
    protected static final int NO_CACHE = -1;
    protected static final int MAX_AGE_15_MINUTES = 60 * 15;
    protected static final int MAX_AGE_10_MINUTES = 60 * 10;
    protected static final int MAX_AGE_5_MINUTES = 60 * 5;
    protected static final int MAX_AGE_1_DAY = 60 * 60 * 24;
    
    /** The logger. */
    @Inject
    private Logger logger;
    
    /**
     * The Enum CacheControlSettings.
     */
    protected enum CacheControlSettings {
        
        /** The maxage include. */
        MAXAGE_INCLUDE,
        
        /** The maxage default. */
        MAXAGE_DEFAULT
    }
    
    
    /**
     * Gets the response.
     *
     * @param request the request
     * @param data the data
     * @param cacheMaxAgeInSeconds the cache max age in seconds
     * @return the response
     */
    protected Response getResponse(Request request, Object data, int cacheMaxAgeInSeconds) {
        CacheControlSettings settings;
        if(cacheMaxAgeInSeconds < 0) {
            settings = CacheControlSettings.MAXAGE_DEFAULT;
        } else {
            settings = CacheControlSettings.MAXAGE_INCLUDE;
        }
        CacheControl cacheControl = getCacheControl(settings, cacheMaxAgeInSeconds);

        String hashCode = Integer.toString(data == null ? "".hashCode() : data.hashCode());
        EntityTag entityTag = new EntityTag(hashCode);
        ResponseBuilder builder = request.evaluatePreconditions(entityTag);

        if(builder == null) {
            builder = Response.ok(data);
        }
        
        builder.cacheControl(cacheControl);
        builder.tag(entityTag);
        Response response = builder.build();

        logger.debug("HTTP STATUS CODE: {} - HASHCODE: {}", response.getStatus(), hashCode);
        
        return response;
    }

    /**
     * Gets the cache control.
     *
     * @param settings the settings
     * @param ageInSeconds the age in seconds
     * @return the cache control
     */
    protected CacheControl getCacheControl(CacheControlSettings settings, int ageInSeconds) {
        CacheControl cc = new CacheControl();
        // If resource is younger than max age, then the browser will always use cache version. 
        // IF resource is older than max age, then a request is sent to the server. 304 may then be returned in case the resource is unmodified.  
        // 15 minutes chosen because vessels should be able to provoke a refresh, if they know a new report is available 
        
        if(settings == CacheControlSettings.MAXAGE_INCLUDE) {
            cc.setMaxAge(ageInSeconds);
            logger.debug("CacheControl is activated for this service and maxAge is -> " + ageInSeconds);
        }
        if(settings == CacheControlSettings.MAXAGE_DEFAULT) {
            cc.setNoCache(true);
            logger.debug("CacheControl is NOT activated for this service.");
        }
        cc.setPrivate(false);
        cc.setNoTransform(false);
        
        return cc;
    }

}
