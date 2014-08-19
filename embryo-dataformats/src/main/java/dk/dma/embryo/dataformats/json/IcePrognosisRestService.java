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

import dk.dma.embryo.dataformats.netcdf.NetCDFResult;
import dk.dma.embryo.dataformats.service.IcePrognosisService;

@Path("/ice")
public class IcePrognosisRestService {
    @Inject
    private IcePrognosisService icePrognosisService;

    @Inject
    private Logger logger;

    @GET
    @Path("/prognoses")
    @Produces("application/json")
    @GZIP
    @NoCache
    public List<String> listIcePrognoses() {
        logger.debug("listIcePrognoses()");
        return icePrognosisService.listAvailableIcePrognoses();
    }

    @GET
    @Path("/prognoses/{id}")
    @Produces("application/json")
    @GZIP
    @NoCache
    public NetCDFResult getIcePrognosis(@PathParam(value = "id") String id) {
        logger.debug("getIcePrognosis()");

        return icePrognosisService.getPrognosis(id);
    }

}
