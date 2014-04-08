/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.arcticweb.reporting.json;

import java.io.Serializable;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.arcticweb.reporting.model.GreenposSearch;
import dk.dma.arcticweb.reporting.service.GreenPosService;
import dk.dma.embryo.vessel.json.Details;
import dk.dma.embryo.vessel.json.VesselDetails;

@Details
@Interceptor
public class GreenposVesselDetailsAmendment implements Serializable {

    private static final long serialVersionUID = 2883332760355286214L;

    @Inject
    private GreenPosService greenposService;

    Logger logger = LoggerFactory.getLogger(GreenposVesselDetailsAmendment.class);
    
    @AroundInvoke
    public Object amendDetails(InvocationContext invocationContext) throws Exception {
        
        logger.debug("amendDetails");
        
        VesselDetails result = (VesselDetails) invocationContext.proceed();
        GreenposSearch s = new GreenposSearch(null, result.getMmsi(), null, null, null, 0, 1);
        boolean greenpos = greenposService.findReports(s).size() > 0;
        result.getAdditionalInformation().put("greenpos", greenpos);
        return result;
    }
}
