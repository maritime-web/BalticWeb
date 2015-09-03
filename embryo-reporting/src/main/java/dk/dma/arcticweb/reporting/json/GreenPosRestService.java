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

import dk.dma.arcticweb.reporting.json.model.GreenPos;
import dk.dma.arcticweb.reporting.json.model.GreenPosShort;
import dk.dma.arcticweb.reporting.json.model.GreenposRequest;
import dk.dma.arcticweb.reporting.model.GreenPosReport;
import dk.dma.arcticweb.reporting.model.GreenposMinimal;
import dk.dma.arcticweb.reporting.model.GreenposSearch;
import dk.dma.arcticweb.reporting.service.GreenPosService;
import dk.dma.embryo.common.json.AbstractRestService;
import dk.dma.embryo.common.util.DateTimeConverter;
import dk.dma.embryo.user.security.Subject;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author Jesper Tejlgaard
 */
@Path("/greenpos")
public class GreenPosRestService extends AbstractRestService {

    @Inject
    private GreenPosService reportingService;

    @Inject
    private Subject subject;

    @Inject
    private Logger logger;

    public GreenPosRestService() {
    }

    @POST
    @Path("/save")
    @Consumes("application/json")
    @Produces("application/json")
    public String save(GreenposRequest request) {
        logger.info("save({})", request);

        GreenPosReport toBeSaved = GreenPosReport.from(request.getReport());
        reportingService.saveReport(toBeSaved, request.getActiveRoute().getRouteId(), request.getActiveRoute().getActive(), request.getIncludeActiveRoute(), request.getReport().getRecipient());

        String email = subject.getUser().getEmail();

        logger.info("save() : {}", email);
        return email;
    }

    @GET
    @Path("/latest/{mmsi}")
    @Produces("application/json")
    @GZIP
    public Response latest(@Context Request request, @PathParam("mmsi") Long mmsi) {
        
        logger.info("latest({})", mmsi);

        GreenPos result = null;

        GreenPosReport report = reportingService.getLatest(mmsi);

        if (report != null) {
            result = report.toJsonModel();
        }

        logger.info("latest({}) - {}", mmsi, result);

        return super.getResponse(request, result, NO_CACHE);
    }

    @GET
    @Path("/latest")
    @Produces("application/json")
    @GZIP
    public Response listLatest(@Context Request request) {
        logger.info("listLatest()");

        List<GreenposMinimal> reports = reportingService.getLatest();

        logger.info("listLatest() - {}", reports);

        return super.getResponse(request, reports, NO_CACHE);
    }


    @GET
    @Path("/{id}")
    @Produces("application/json")
    @GZIP
    public Response get(
            @Context Request request,
            @PathParam("id") String id) {
        
        logger.info("get({})", id);

        GreenPosReport report = reportingService.get(id);

        GreenPos result = report.toJsonModel();

        logger.info("get() - {}", result);
        
        return super.getResponse(request, result, NO_CACHE);
    }

    @GET
    @Path("/list")
    @Produces("application/json")
    @GZIP
    @NoCache
    public Response list(
            @Context Request request,
            @QueryParam("type") String type, 
            @QueryParam("mmsi") Long mmsi,
            @QueryParam("ts") String ts, 
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortOrder") String sortOrder, 
            @QueryParam("start") Integer start,
            @QueryParam("max") Integer max) {
        
        logger.info("list({})");

        DateTime dateTime = null;
        if (ts != null && ts.trim().length() > 0) {
            try {
                long lts = Long.parseLong(ts);
                dateTime = new DateTime(lts, DateTimeZone.UTC);
            } catch (NumberFormatException e) {
                dateTime = DateTimeConverter.getDateTimeConverter().toObject(ts);
            }
        }

        GreenposSearch search = new GreenposSearch(type, mmsi, dateTime, sortBy, sortOrder, start, max);

        logger.info("Searching with {}", search);

        List<GreenPosReport> reports = reportingService.findReports(search);

        GreenPosShort[] result = GreenPosReport.toJsonModelShort(reports);

        logger.info("list() - {}", (Object[]) result);
     
        return super.getResponse(request, result, NO_CACHE);
    }
}
