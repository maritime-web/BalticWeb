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

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;

import dk.dma.arcticweb.service.GreenPosService;
import dk.dma.embryo.domain.GreenPosReport;
import dk.dma.embryo.domain.GreenposSearch;
import dk.dma.embryo.rest.json.GreenPos;
import dk.dma.embryo.rest.util.DateTimeConverter;

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
    @Path("/latest/{shipMaritimeId}")
    @Produces("application/json")
    public GreenPos latest(@PathParam("shipMaritimeId") String shipMaritimeId) {
        logger.debug("latest({})", shipMaritimeId);

        GreenPos result = null;

        GreenPosReport report = reportingService.getLatest(shipMaritimeId);

        if (report != null) {
            result = report.toJsonModel();
        }

        logger.debug("latest({}) - {}", shipMaritimeId, result);

        return result;
    }

    @GET
    @Path("/list")
    @Produces("application/json")
    public GreenPos[] list(@QueryParam("name") String type, @QueryParam("name") String name,
            @QueryParam("mmsi") Long mmsi, @QueryParam("callSign") String callSign,
            @QueryParam("reporter") String reporter, @QueryParam("ts") String ts, @QueryParam("sortBy") String sortBy,
            @QueryParam("sortOrder") String sortOrder, @QueryParam("start") Integer start, @QueryParam("max") Integer max) {
        logger.debug("list({})");

        LocalDateTime dateTime = null;
        if (ts != null && ts.trim().length() > 0) {
            dateTime = DateTimeConverter.getDateTimeConverter().toObject(ts);
        }

        List<GreenPosReport> reports = reportingService.findReports(new GreenposSearch(type, name, mmsi, callSign,
                reporter, dateTime, sortBy, sortOrder, start, max));

        GreenPos[] result = GreenPosReport.toJsonModel(reports);

        logger.debug("list() - {}", (Object[]) result);
        return result;
    }
}
