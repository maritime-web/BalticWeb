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
package dk.dma.embryo.metoc.json;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import dk.dma.embryo.metoc.json.client.DmiSejlRuteService.SejlRuteResponse;
import dk.dma.embryo.metoc.service.MetocService;

/**
 * 
 * @author Jesper Tejlgaard
 */
@Path("/metoc")
public class MetocRestService {
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
    @NoCache
    public List<Metoc> getMetoc(@PathParam("routeIds") String routeIds) {
        logger.debug("getMetoc({})", routeIds);
  
        String[] ids = routeIds.split(":");
        
        SejlRuteResponse[] sejlRuteResponses = metocService.listMetocs(ids);

        List<Metoc> metocs = new ArrayList<>(sejlRuteResponses.length);
        for(SejlRuteResponse sejlRuteResponse : sejlRuteResponses){
            if(sejlRuteResponse.getMetocForecast() != null){
                metocs.add(Metoc.from(sejlRuteResponse.getMetocForecast()));
            }
        }
        
        logger.debug("getMetoc({}) : {}", ids, metocs);
        return metocs;
    }
}
