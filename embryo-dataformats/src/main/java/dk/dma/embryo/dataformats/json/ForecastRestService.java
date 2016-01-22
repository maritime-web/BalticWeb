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

import dk.dma.embryo.common.json.AbstractRestService;
import dk.dma.embryo.dataformats.persistence.ForecastDataRepository;
import dk.dma.embryo.dataformats.service.ForecastService;
import org.jboss.resteasy.annotations.GZIP;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

/**
 * Every REST call regarding NetCDF based forecasts from the client will end up
 * in here.
 * 
 * Note that "forecasts" were originally named "prognoses", which is still
 * apparent in legacy code here and there.
 * 
 * @author avlund
 *
 */
@Path("/forecasts")
public class ForecastRestService extends AbstractRestService {
    
    @Inject
    private ForecastService forecastService;

    @Inject
    private ForecastDataRepository forecastDataRepository;

    @Inject
    private Logger logger;

    @GET
    @Path("/ice")
    @Produces("application/json")
    @GZIP
    public Response listIcePrognoses(@Context Request request) {
        logger.debug("listIcePrognoses()");
        return super.getResponse(request, forecastService.listAvailableIceForecasts(), NO_CACHE);
    }

    @GET
    @Path("/ice/{id}")
    @Produces("application/json")
    @GZIP
    public Response getIcePrognosis(@PathParam(value = "id") String id, @Context Request request) {
        logger.debug("getIcePrognosis({})", id);
        String data = forecastDataRepository.getForecastData(id);
        return super.getResponse(request, data, MAX_AGE_1_DAY);
    }

    @GET
    @Path("/waves")
    @Produces("application/json")
    @GZIP
    public Response listWavePrognoses(@Context Request request) {
        logger.debug("listWavePrognoses()");
        return super.getResponse(request, forecastService.listAvailableWaveForecasts(), NO_CACHE);
    }

    @GET
    @Path("/waves/{id}")
    @Produces("application/json")
    @GZIP
    public Response getWavePrognosis(@PathParam(value = "id") String id, @Context Request request) {
        logger.debug("getWavePrognosis({})", id);
        String data = forecastDataRepository.getForecastData(id);
        return getResponse(request, data, MAX_AGE_1_DAY);
    }

    @GET
    @Path("/currents")
    @Produces("application/json")
    @GZIP
    public Response listCurrentPrognoses(@Context Request request) {
        logger.debug("listCurrentPrognoses()");
        return super.getResponse(request, forecastService.listAvailableCurrentForecasts(), NO_CACHE);
    }

    @GET
    @Path("/currents/{id}")
    @Produces("application/json")
    @GZIP
    public Response getCurrentPrognosis(@PathParam(value = "id") String id, @Context Request request) {
        logger.debug("getCurrentPrognosis({})", id);

        String data = forecastDataRepository.getForecastData(id);
        return getResponse(request, data, MAX_AGE_1_DAY);
    }
}
