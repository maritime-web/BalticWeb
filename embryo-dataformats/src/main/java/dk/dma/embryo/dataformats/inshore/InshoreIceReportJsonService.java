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
package dk.dma.embryo.dataformats.inshore;

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

@Path("/inshore-ice-report")
public class InshoreIceReportJsonService extends AbstractRestService {

    @Inject
    private InshoreIceReportService iceInformationService;

    @Inject
    private Logger logger;

    @GET
    @Path("/provider/{provider}")
    @Produces("application/json")
    @GZIP
    public Response inshoreIceReport(@PathParam("provider") String providerKey, @Context Request request) {
        logger.info("iceInformations({})", providerKey);

        InshoreIceReportMerged report = iceInformationService.getInshoreIceReportsMerged(providerKey);

        return super.getResponse(request, report, MAX_AGE_15_MINUTES);
    }
}
