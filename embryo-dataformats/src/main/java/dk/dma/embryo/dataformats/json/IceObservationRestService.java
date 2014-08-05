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

import dk.dma.embryo.dataformats.model.InshoreIceReport;
import dk.dma.embryo.dataformats.model.IceObservation;
import dk.dma.embryo.dataformats.model.Provider;
import dk.dma.embryo.dataformats.service.InshoreIceReportService;
import dk.dma.embryo.dataformats.service.IceObservationService;

@Path("/ice")
public class IceObservationRestService {
    @Inject
    private IceObservationService iceObservationService;

    @Inject
    private InshoreIceReportService iceInformationService;

    
    @Inject
    private Logger logger;
    
    

    
    @GET
    @Path("/provider/list")
    @Produces("application/json")
    @GZIP
    @NoCache
    public List<Provider> listIceChartProviders() {
        return iceObservationService.listIceChartProviders();
    }

    @GET
    @Path("/provider/{charttype}/{provider}/observations")
    @Produces("application/json")
    @GZIP
    @NoCache
    public List<IceObservation> listIceObservations(@PathParam("charttype") String chartType, @PathParam("provider") String providerKey) {
        logger.debug("listIceObservations({})", providerKey);
        
        return iceObservationService.listAvailableIceObservations(chartType, providerKey);
    }

    @GET
    @Path("/provider/{provider}/inshoreicereport")
    @Produces("application/json")
    @GZIP
    @NoCache
    public InshoreIceReport inshoreIceReport(@PathParam("provider") String providerKey) {
        logger.debug("iceInformations({})", providerKey);
        return iceInformationService.getInshoreIceReport(providerKey);
    }
}
