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
package dk.dma.arcticweb.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import dk.dma.arcticweb.service.IceObservationService;
import dk.dma.embryo.domain.IceObservation;
import dk.dma.embryo.domain.Provider;

@Path("/ice")
public class IceObservationRestService {
    @Inject
    private IceObservationService iceObservationService;

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
    @Path("/provider/{provider}/observations")
    @Produces("application/json")
    @GZIP
    @NoCache
    public List<IceObservation> listIceObservations(@PathParam("provider") String providerKey) {
        logger.debug("listIceObservations({})", providerKey);
        
        return iceObservationService.listAvailableIceObservations(providerKey);
    }
}
