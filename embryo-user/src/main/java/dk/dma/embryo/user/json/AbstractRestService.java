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
package dk.dma.embryo.user.json;

import javax.inject.Inject;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;

public abstract class AbstractRestService {

    @Inject
    private Logger logger;
    
    protected enum CacheControlSettings {
        MAXAGE_INCLUDE,
        MAXAGE_DEFAULT
    }
    
    protected Response getResponse(Request request, Object data, int cacheMaxAgeInSeconds) {
        CacheControlSettings settings;
        if(cacheMaxAgeInSeconds < 1) {
            settings = CacheControlSettings.MAXAGE_DEFAULT;
        } else {
            settings = CacheControlSettings.MAXAGE_INCLUDE;
        }
        CacheControl cacheControl = getCacheControl(settings, cacheMaxAgeInSeconds);
        
        EntityTag entityTag = new EntityTag(Integer.toString(data.hashCode()));
        ResponseBuilder builder = request.evaluatePreconditions(entityTag);

        if(builder == null) {
            builder = Response.ok(data);
        }
        
        builder.cacheControl(cacheControl);
        builder.tag(entityTag);
        Response response = builder.build();
        return response;
    }

    protected CacheControl getCacheControl(CacheControlSettings settings, int ageInSeconds) {
        CacheControl cc = new CacheControl();
        // If resource is younger than max age, then the browser will always use cache version. 
        // IF resource is older than max age, then a request is sent to the server. 304 may then be returned in case the resource is unmodified.  
        // 15 minutes chosen because vessels should be able to provoke a refresh, if they know a new report is available 
        
        if(settings == CacheControlSettings.MAXAGE_INCLUDE) {
            cc.setMaxAge(ageInSeconds);
            logger.info("CacheControl is activated for this service and maxAge is -> " + ageInSeconds);
        }
        if(settings == CacheControlSettings.MAXAGE_DEFAULT) {
            cc.setMaxAge(-1);
            logger.info("CacheControl is NOT activated for this service.");
        }
        cc.setPrivate(false);
        cc.setNoTransform(false);
        
        return cc;
    }

}
