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
package dk.dma.arcticweb.reporting.json;

import dk.dma.arcticweb.reporting.model.GreenposSearch;
import dk.dma.arcticweb.reporting.service.GreenPosService;
import dk.dma.embryo.vessel.json.Details;
import dk.dma.embryo.vessel.json.VesselDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ws.rs.core.Response;
import java.io.Serializable;

@Details
@Interceptor
public class GreenposVesselDetailsAmendment implements Serializable {

    private static final long serialVersionUID = 2883332760355286214L;

    @Inject
    private GreenPosService greenposService;

    Logger logger = LoggerFactory.getLogger(GreenposVesselDetailsAmendment.class);
    
    @AroundInvoke
    public Object amendDetails(InvocationContext invocationContext) throws Exception {
        logger.trace("amendDetails(x)");

        Response serverResponse = (Response) invocationContext.proceed();
        VesselDetails result = (VesselDetails) serverResponse.getEntity();

        if(result != null) {
            Long mmsi = result.getMmsiNumber();
            if (mmsi != null) {
                GreenposSearch s = new GreenposSearch(null, mmsi, null, null, null, 0, 1);
                boolean greenpos = greenposService.findReports(s).size() > 0;
                result.getAdditionalInformation().put("greenpos", greenpos);
            }
        }
        return serverResponse;
    }

}
