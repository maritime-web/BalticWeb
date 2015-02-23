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
package dk.dma.embryo.metoc.json;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.GZIP;
import org.slf4j.Logger;

import dk.dma.embryo.common.json.AbstractRestService;
import dk.dma.embryo.metoc.json.client.DmiSejlRuteService.SejlRuteResponse;
import dk.dma.embryo.metoc.service.MetocService;

/**
 * 
 * @author Jesper Tejlgaard
 */
@Path("/metoc")
public class MetocRestService extends AbstractRestService {
    
    @Inject
    private MetocService metocService;

    @Inject
    private Logger logger;

    public MetocRestService() {
    }

    @GET
    @Path("/list/{routeIds}")
    @Produces("application/json")
    @GZIP
    public Response getMetocs(
        @Context Request request,
        @PathParam("routeIds") String routeIds) {
        
        logger.info("getMetocs({})", routeIds);

        String[] ids = routeIds.split(":");

        SejlRuteResponse[] sejlRuteResponses = metocService.listMetocs(ids);

        List<Metoc> metocs = new ArrayList<>(sejlRuteResponses.length);
        for (SejlRuteResponse sejlRuteResponse : sejlRuteResponses) {
            if (sejlRuteResponse.getMetocForecast() != null) {
                metocs.add(Metoc.from(sejlRuteResponse.getMetocForecast(), new ForecastPredicate()));
            }
        }

        logger.info("getMetocs({}) : {}", ids, metocs);
        
        return super.getResponse(request, metocs, NO_CACHE);
    }
}
