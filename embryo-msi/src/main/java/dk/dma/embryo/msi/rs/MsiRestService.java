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
package dk.dma.embryo.msi.rs;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.GZIP;
import org.slf4j.Logger;

import dk.dma.embryo.common.json.AbstractRestService;
import dk.dma.embryo.msi.MsiClient;
import dk.dma.embryo.msi.MsiClient.MsiItem;

@Path("/msi")
public class MsiRestService extends AbstractRestService {
    
    @Inject
    private Logger logger;
    
    @Inject
    private MsiClient msiClient;

    @GET
    @Path("/list")
    @Produces("application/json")
    @GZIP
    /**
     * 
     * @param regions
     * @return List of MSI warnings
     */
    public Response listActiveWarnings(@Context Request request, @QueryParam("regions") List<String> regions) {
        
        List<MsiItem> activeWarnings = msiClient.getActiveWarnings(regions);

        return super.getResponse(request, activeWarnings, NO_CACHE); 
    }
    
    @GET
    @Path("/regions")
    @Produces("application/json")
    @GZIP
    public Response getRegions(@Context Request request) {
        logger.info("getRegions called from MsiRestService.");
        return super.getResponse(request, msiClient.getRegions(), NO_CACHE);
    }
}
