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
package dk.dma.embryo.common.log;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;

import dk.dma.embryo.common.json.AbstractRestService;

@Path("/log")
public class LogEntryRestService extends AbstractRestService {
    @Inject
    private LogEntryDao logEntryDao;
    
    @Inject
    private Logger logger;

    @GET
    @Path("/search")
    @Produces("application/json")
    @GZIP
    public Response search(
        @Context Request request,
        @QueryParam("service") String service, 
        @QueryParam("count") Integer count, 
        @QueryParam("from") Long from) {
        
        logger.info("search({}, {}, {})", service, count, from);

        DateTime ts = new DateTime(from, DateTimeZone.UTC);
        List<dk.dma.embryo.common.log.LogEntry> result = logEntryDao.search(service, count, ts);
        List<JsonLogEntry> transformed = LogEntry.fromJsonModel(result);
        
        logger.info("search() {}: ", transformed);
        
        return super.getResponse(request, transformed, NO_CACHE);
    }

    /*
     * Used for Zabbix surveillance.
     */
    @GET
    @Path("/latest")
    @Produces("application/json")
    @NoCache
    public Response latest(
        @Context Request request,
        @QueryParam("service") String service) {
        
        logger.info("latest({})", service);
        
        dk.dma.embryo.common.log.LogEntry latest = logEntryDao.latest(service); 
        JsonLogEntry result = latest == null ? null : latest.toJsonModel();
        
        logger.info("latest({}) : {}", service, result);
        
        return Response.ok(result).build();
    }

    @GET
    @Path("/services")
    @Produces("application/json")
    public Response services(
        @Context Request request,
        @QueryParam("from") Long from) {
        
        logger.info("services()");
        
        DateTime fromTs;
        if(from == null){
            fromTs = DateTime.now(DateTimeZone.UTC).minusMonths(1);
        }else{
            fromTs = new DateTime(from, DateTimeZone.UTC);
        }
        
        List<String> services = logEntryDao.services(fromTs); 
        
        logger.info("services():{}", services);
        
        return super.getResponse(request, services, NO_CACHE);
    }
}
