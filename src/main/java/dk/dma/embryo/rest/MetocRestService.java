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
package dk.dma.embryo.rest;

import java.util.Date;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.jboss.resteasy.annotations.GZIP;
import org.slf4j.Logger;

import dk.dma.embryo.rest.json.Metoc;
import dk.dma.embryo.restclients.DmiSejlRuteService;
import dk.dma.embryo.restclients.DmiSejlRuteService.SejlRuteResponse;
import dk.dma.embryo.service.MetocService;

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
    
    @Inject
    DmiSejlRuteService dmiSejlRuteService;
    
    public MetocRestService() {
    }

    @GET
    @Path("/{routeId}")
    @Produces("application/json")
    @GZIP
    public Metoc getMetoc(@PathParam("routeId") String id) {
        logger.debug("getMetoc({})", id);
  
        SejlRuteResponse sejlRuteResponse = metocService.getMetoc(id);

        Metoc metoc = null; 
        if(sejlRuteResponse.getMetocForecast() != null){
            metoc = Metoc.from(sejlRuteResponse.getMetocForecast());
        }

        logger.debug("getMetoc({}) : {}", id, metoc);
        return metoc;
    }
}
