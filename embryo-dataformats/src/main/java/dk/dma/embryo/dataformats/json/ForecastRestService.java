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
package dk.dma.embryo.dataformats.json;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import dk.dma.embryo.dataformats.model.Forecast;
import dk.dma.embryo.dataformats.service.ForecastService;

@Path("/forecasts")
public class ForecastRestService {
    @Inject
    private ForecastService forecastService;

    @Inject
    private Logger logger;

    @GET
    @Path("/ice")
    @Produces("application/json")
    @GZIP
    @NoCache
    public List<Forecast> listIcePrognoses() {
        logger.debug("listIcePrognoses()");
        return forecastService.listAvailableIceForecasts();
    }

    @GET
    @Path("/ice/{id}")
    @Produces("application/json")
    @GZIP
    @NoCache
    public Response getIcePrognosis(@PathParam(value = "id") long id, @Context Request request) {
        logger.debug("getIcePrognosis()");
        String data = forecastService.getForecast(id).getData();
        return getResponse(request, data);
    }

    @GET
    @Path("/waves")
    @Produces("application/json")
    @GZIP
    @NoCache
    public List<Forecast> listWavePrognoses() {
        logger.debug("listWavePrognoses()");
        return forecastService.listAvailableWaveForecasts();
    }

    @GET
    @Path("/waves/{id}")
    @Produces("application/json")
    @GZIP
    @NoCache
    public Response getWavePrognosis(@PathParam(value = "id") long id, @Context Request request) {
        logger.debug("getWavePrognosis()");
        String data = forecastService.getForecast(id).getData();
        return getResponse(request, data);
    }

    @GET
    @Path("/currents")
    @Produces("application/json")
    @GZIP
    @NoCache
    public List<Forecast> listCurrentPrognoses() {
        logger.debug("listCurrentPrognoses()");
        return forecastService.listAvailableCurrentForecasts();
    }

    @GET
    @Path("/currents/{id}")
    @Produces("application/json")
    @GZIP
    @NoCache
    public Response getCurrentPrognosis(@PathParam(value = "id") long id, @Context Request request) {
        logger.debug("getWavePrognosis()");
        
        String data = forecastService.getForecast(id).getData();
        return getResponse(request, data);
    }
    
    private Response getResponse(Request request, String data) {
        EntityTag entityTag = new EntityTag(Integer.toString(data.hashCode()));
        ResponseBuilder builder = request.evaluatePreconditions(entityTag);
        if(builder == null) {
            builder = Response.ok(data);
        }
        builder.cacheControl(getCacheControl());
        builder.tag(entityTag);
        Response response = builder.build();
        return response;
    }
    
    private CacheControl getCacheControl() {
        CacheControl cc = new CacheControl();
        // If resource is younger than max age, then the browser will always use cache version. 
        // IF resource is older than max age, then a request is sent to the server. 304 may then be returned in case the resource is unmodified.  
        // 15 minutes chosen because vessels should be able to provoke a refresh, if they know a new report is available 
        cc.setMaxAge(60 * 15);
        cc.setPrivate(false);
        cc.setNoTransform(false);
        return cc;
    }

    
}
