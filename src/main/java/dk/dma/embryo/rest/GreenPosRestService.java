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

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.slf4j.Logger;

import dk.dma.arcticweb.service.GreenPosService;
import dk.dma.embryo.domain.GreenPosReport;
import dk.dma.embryo.rest.json.GreenPos;

/**
 * 
 * @author Jesper Tejlgaard
 */
@Path("/greenpos")
public class GreenPosRestService {

    @Inject
    private GreenPosService reportingService;

    @Inject
    private Logger logger;

    public GreenPosRestService() {
    }

    @POST
    @Consumes("application/json")
    public void save(GreenPos report) {
        logger.debug("save({})", report);
        
        GreenPosReport toBeSaved = GreenPosReport.from(report);
        
        reportingService.saveReport(toBeSaved);
        // String result = "Product created : " + product;
        // return Response.status(201).entity(result).build();
        logger.debug("save() - done", report);
    }


    @GET
    @Path("/list")
    @Produces("application/json")
    public GreenPos[] list() {
        logger.debug("findReports()");

        List<GreenPosReport> reports = reportingService.findReports();
        
        GreenPos[] result = GreenPosReport.toJsonModel(reports);
     
        logger.debug("findReports() - {}", result);
        
        return result;
    }
}
