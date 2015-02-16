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
package dk.dma.embryo.dataformats.json;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.Cache;
import org.joda.time.DateTime;
import org.slf4j.Logger;

import dk.dma.embryo.dataformats.model.ShapeFileMeasurement;
import dk.dma.embryo.dataformats.model.factory.ShapeFileNameParserFactory;
import dk.dma.embryo.dataformats.persistence.ShapeFileMeasurementDao;
import dk.dma.embryo.dataformats.service.ShapeFileService;
import dk.dma.embryo.dataformats.service.ShapeFileService.Shape;

@Path("/shapefile")
public class ShapeFileRestService {

    @Inject
    Logger logger;

    @Inject
    ShapeFileService shapeFileService;

    @Inject
    ShapeFileMeasurementDao dao;

    @Inject
    ShapeFileNameParserFactory factory;

    public ShapeFileRestService() {
        super();
    }
    
    public ShapeFileRestService(ShapeFileService service, ShapeFileMeasurementDao dao, ShapeFileNameParserFactory factory){
        this.shapeFileService = service;
        this.dao = dao;
        this.factory = factory;
    }

    @GET
    @Path("/static/single/{id}")
    @Produces("application/json")
    @GZIP
    @Cache(maxAge = 31556926, isPrivate = false)
    public Shape getSingleFile(
            @PathParam("id") String id, 
            @DefaultValue("0") @QueryParam("resolution") int resolution,
            @DefaultValue("") @QueryParam("filter") String filter,
            @DefaultValue("false") @QueryParam("delta") boolean delta,
            @DefaultValue("2") @QueryParam("exponent") int exponent, 
            @DefaultValue("0") @QueryParam("parts") int parts)
            throws IOException {
        
        logger.info("Request for single file: {}", id);
        try {
            return shapeFileService.readSingleFile(id, resolution, filter, delta, exponent, parts);
        } catch (FileNotFoundException e) {
            throw new WebApplicationException(Response.Status.GONE);
        }
    }

    @HEAD
    @GET
    @Path("/static/multiple/{ids}")
    @Produces("application/json")
    @GZIP
    @Cache(maxAge = 31556926, isPrivate = false)
    public List<Shape> getMultipleFile(@PathParam("ids") String ids,
            @DefaultValue("0") @QueryParam("resolution") int resolution,
            @DefaultValue("") @QueryParam("filter") String filter,
            @DefaultValue("false") @QueryParam("delta") boolean delta,
            @DefaultValue("2") @QueryParam("exponent") int exponent, @DefaultValue("0") @QueryParam("parts") int parts)
            throws IOException {
        logger.info("getMultipleFile({}, {}, {}, {}, {}, {})", ids, resolution, filter, delta, exponent, parts);

        try {
            List<Shape> result = new ArrayList<>();
            for (String id : ids.split(",")) {
                result.add(shapeFileService.readSingleFile(id, resolution, filter, delta, exponent, parts));
            }
            return result;
        } catch (FileNotFoundException e) {
            throw new WebApplicationException(Response.Status.GONE);
        }
    }

    private CacheControl getCacheControl() {
        CacheControl cc = new CacheControl();
        // 15 minutter
        cc.setMaxAge(60*15);
        cc.setPrivate(false);
        cc.setNoTransform(false);
        return cc;
    }

    @GET
    @Path("/single/{id}")
    @Produces("application/json")
    @GZIP
    public Response getSingleFile(@PathParam("id") String id,
            @QueryParam("resolution") Integer resolution,
            @DefaultValue("") @QueryParam("filter") String filter,
            @DefaultValue("false") @QueryParam("delta") boolean delta,
            @QueryParam("exponent") Integer exponent, @QueryParam("parts") Integer parts,
            @Context Request request) throws IOException {
        logger.info("Request for single file: {}", id);

        try {
            CacheControl cc = getCacheControl();

            int dashIndex = id.indexOf("-");
            int dotIndex = id.indexOf(".");
            String chartType = id.substring(0, dashIndex);
            String provider = id.substring(dashIndex + 1, dotIndex);
            String chart = id.substring(dotIndex + 1);
            ShapeFileMeasurement measurement = dao.lookup(chart, chartType, provider);
            DateTime lastModified = measurement.getCreated();

            ResponseBuilder builder = request.evaluatePreconditions(lastModified.toDate());

            // cached resource did change -> serve updated content
            if (builder == null) {
                String file = id + (measurement.getVersion() > 0 ? "_v" + measurement.getVersion() : ""); 
                logger.debug("looking up shape file {}", file);
                Shape shape = shapeFileService.readSingleFile(file, resolution, filter, delta, exponent, parts);
                builder = Response.ok(shape);
            }

            builder.lastModified(lastModified.toCalendar(Locale.ENGLISH).getTime());
            
            builder.cacheControl(cc);
            return builder.build();

        } catch (FileNotFoundException e) {
            throw new WebApplicationException(Response.Status.GONE);
        }
    }

}
