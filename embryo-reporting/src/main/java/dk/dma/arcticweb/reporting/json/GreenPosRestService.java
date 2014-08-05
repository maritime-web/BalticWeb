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

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;

import dk.dma.arcticweb.reporting.json.model.GreenPos;
import dk.dma.arcticweb.reporting.json.model.GreenPosShort;
import dk.dma.arcticweb.reporting.json.model.GreenposRequest;
import dk.dma.arcticweb.reporting.model.GreenPosReport;
import dk.dma.arcticweb.reporting.model.GreenposMinimal;
import dk.dma.arcticweb.reporting.model.GreenposSearch;
import dk.dma.arcticweb.reporting.service.GreenPosService;
import dk.dma.embryo.common.util.DateTimeConverter;
import dk.dma.embryo.user.security.Subject;

/**
 * 
 * @author Jesper Tejlgaard
 */
@Path("/greenpos")
public class GreenPosRestService {

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
        logger.debug("save({})", request);

        GreenPosReport toBeSaved = GreenPosReport.from(request.getReport());
        reportingService.saveReport(toBeSaved, request.getActiveRoute().getRouteId(), request.getActiveRoute().getActive(), request.getIncludeActiveRoute(), request.getReport().getRecipients());
        
        String email = subject.getUser().getEmail();
        
        logger.debug("save() : {}" , email);
        return email;
    }

    @GET
    @Path("/latest/{mmsi}")
    @Produces("application/json")
    @GZIP
    @NoCache
    public GreenPos latest(@PathParam("mmsi") Long mmsi) {
        logger.debug("latest({})", mmsi);

        GreenPos result = null;

        GreenPosReport report = reportingService.getLatest(mmsi);

        if (report != null) {
            result = report.toJsonModel();
        }

        logger.debug("latest({}) - {}", mmsi, result);

        return result;
    }

    @GET
    @Path("/latest")
    @Produces("application/json")
    @GZIP
    @NoCache
    public List<GreenposMinimal> listLatest() {
        logger.debug("listLatest()");

        List<GreenposMinimal> reports = reportingService.getLatest();

        logger.debug("listLatest() - {}", reports);

        return reports;
    }

    
    @GET
    @Path("/{id}")
    @Produces("application/json")
    @GZIP
    @NoCache
    public GreenPos get(@PathParam("id") String id) {
        logger.debug("get({})", id);

        GreenPosReport report = reportingService.get(id);

        GreenPos result = report.toJsonModel();

        logger.debug("get() - {}", result);
        return result;
    }

    @GET
    @Path("/list")
    @Produces("application/json")
    @GZIP
    @NoCache
    public GreenPosShort[] list(@QueryParam("type") String type, @QueryParam("mmsi") Long mmsi,
            @QueryParam("ts") String ts, @QueryParam("sortBy") String sortBy,
            @QueryParam("sortOrder") String sortOrder, @QueryParam("start") Integer start,
            @QueryParam("max") Integer max) {
        logger.debug("list({})");

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

        logger.debug("Searching with {}", search);

        List<GreenPosReport> reports = reportingService.findReports(search);

        GreenPosShort[] result = GreenPosReport.toJsonModelShort(reports);

        logger.debug("list() - {}", (Object[]) result);
        return result;
    }
}
