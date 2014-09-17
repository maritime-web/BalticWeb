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

import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import dk.dma.embryo.dataformats.service.PrognosisService;

@Path("/prognoses")
public class PrognosesRestService {
    @Inject
    private PrognosisService prognosisService;

    @Inject
    private Logger logger;

    @GET
    @Path("/ice")
    @Produces("application/json")
    @GZIP
    @NoCache
    public List<String> listIcePrognoses() {
        logger.debug("listIcePrognoses()");
        return prognosisService.listAvailableIcePrognoses();
    }

    @GET
    @Path("/ice/{id}")
    @Produces("application/json")
    @GZIP
    @NoCache
    public String getIcePrognosis(@PathParam(value = "id") String id) {
        logger.debug("getIcePrognosis()");

        return prognosisService.getIcePrognosis(id);
    }

    @GET
    @Path("/waves")
    @Produces("application/json")
    @GZIP
    @NoCache
    public List<String> listWavePrognoses() {
        logger.debug("listWavePrognoses()");
        return prognosisService.listAvailableWavePrognoses();
    }

    @GET
    @Path("/waves/{id}")
    @Produces("application/json")
    @GZIP
    @NoCache
    public String getWavePrognosis(@PathParam(value = "id") String id) {
        logger.debug("getWavePrognosis()");

        return prognosisService.getWavePrognosis(id);
    }
    
    @GET
    @Path("/currents")
    @Produces("application/json")
    @GZIP
    @NoCache
    public List<String> listCurrentPrognoses() {
        logger.debug("listCurrentPrognoses()");
        return prognosisService.listAvailableCurrentPrognoses();
    }

    @GET
    @Path("/currents/{id}")
    @Produces("application/json")
    @GZIP
    @NoCache
    public String getCurrentPrognosis(@PathParam(value = "id") String id) {
        logger.debug("getWavePrognosis()");

        return prognosisService.getCurrentPrognosis(id);
    }
}
