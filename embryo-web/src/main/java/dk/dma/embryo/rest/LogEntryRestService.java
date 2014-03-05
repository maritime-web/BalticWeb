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
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;

import dk.dma.embryo.dao.LogEntryDao;
import dk.dma.embryo.rest.json.LogEntry;

@Path("/log")
@NoCache
public class LogEntryRestService {
    @Inject
    private LogEntryDao logEntryDao;
    
    @Inject
    private Logger logger;

    @GET
    @Path("/search")
    @Produces("application/json")
    @GZIP
    public List<LogEntry> search(@QueryParam("service") String service, @QueryParam("count") Integer count, @QueryParam("from") Long from) {
        logger.debug("search({}, {}, {})", service, count, from);

        DateTime ts = new DateTime(from, DateTimeZone.UTC);
        List<dk.dma.embryo.domain.LogEntry> result = logEntryDao.search(service, count, ts);
        List<LogEntry> transformed = dk.dma.embryo.domain.LogEntry.fromJsonModel(result);
        logger.debug("search() {}: ", transformed);
        return transformed;
    }

    @GET
    @Path("/latest")
    @Produces("application/json")
    public LogEntry latest(@QueryParam("service") String service) {
        logger.debug("latest({})", service);
        
        dk.dma.embryo.domain.LogEntry latest = logEntryDao.latest(service); 

        LogEntry result = latest == null ? null : latest.toJsonModel();
        
        logger.debug("latest({}) : {}", service, result);
        return result;
    }

    @GET
    @Path("/services")
    @Produces("application/json")
    public List<String> services(@QueryParam("from") Long from) {
        logger.debug("services()");
        
        DateTime fromTs;
        if(from == null){
            fromTs = DateTime.now(DateTimeZone.UTC).minusMonths(1);
        }else{
            fromTs = new DateTime(from, DateTimeZone.UTC);
        }
        
        List<String> services = logEntryDao.services(fromTs); 
        
        logger.debug("services():{}", services);
        return services;
    }
}
