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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.Cache;
import org.slf4j.Logger;

import dk.dma.embryo.service.ShapeFileService;
import dk.dma.embryo.service.ShapeFileService.Shape;

@Path("/shapefile")
public class ShapeFileRestService {

    @Inject
    Logger logger;

    @Inject
    ShapeFileService shapeFileService;

    public ShapeFileRestService() {
        super();
    }

    @GET
    @Path("/single/{id}")
    @Produces("application/json")
    @GZIP
    @Cache(maxAge = 31556926, isPrivate = false)
    public Shape getSingleFile(@PathParam("id") String id, @DefaultValue("0") @QueryParam("resolution") int resolution,
            @DefaultValue("") @QueryParam("filter") String filter,
            @DefaultValue("false") @QueryParam("delta") boolean delta,
            @DefaultValue("2") @QueryParam("exponent") int exponent, @DefaultValue("0") @QueryParam("parts") int parts)
            throws IOException {
        logger.info("Request for single file: {}", id);
        try {
            return shapeFileService.readSingleFile(id, resolution, filter, delta, exponent, parts);
        } catch (FileNotFoundException e) {
            throw new WebApplicationException(Response.Status.GONE);
        }
    }

    @GET
    @Path("/multiple/{ids}")
    @Produces("application/json")
    @GZIP
    @Cache(maxAge = 31556926, isPrivate = false)
    public List<Shape> getMultipleFile(@PathParam("ids") String ids,
            @DefaultValue("0") @QueryParam("resolution") int resolution,
            @DefaultValue("") @QueryParam("filter") String filter,
            @DefaultValue("false") @QueryParam("delta") boolean delta,
            @DefaultValue("2") @QueryParam("exponent") int exponent, @DefaultValue("0") @QueryParam("parts") int parts)
            throws IOException {
        logger.info("Request for multiple files: {}", ids);

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

}
