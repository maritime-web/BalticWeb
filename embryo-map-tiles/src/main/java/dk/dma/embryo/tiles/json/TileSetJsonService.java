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
package dk.dma.embryo.tiles.json;

import dk.dma.embryo.tiles.model.TileSet;
import dk.dma.embryo.tiles.service.TileSetDao;
import org.jboss.resteasy.annotations.GZIP;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.util.List;

@Path("/tileset")
@Named
public class TileSetJsonService {

    @Inject
    Logger logger;

    @Inject
    TileSetDao tileSetDao;

    private CacheControl getCacheControl() {
        CacheControl cc = new CacheControl();
        // 15 minutes
        cc.setMaxAge(60 * 15);
        //cc.setMaxAge(60);
        cc.setPrivate(false);
        cc.setNoTransform(false);
        return cc;
    }

    @GET
    @Path("/list/{type}")
    @Produces("application/json")
    @GZIP
    public Response filter(@PathParam("type") String type, @Context Request request) {
        logger.info("filter()");

        List<TileSet> tileSets = tileSetDao.listByTypeAndStatus(type, TileSet.Status.SUCCESS);
        List<JsonTileSet> result = TileSet.toJsonModel(tileSets);

        EntityTag tag = EntityTag.valueOf(Integer.toString(result.hashCode()));
        ResponseBuilder builder = request.evaluatePreconditions(tag);

        // cached resource did change -> serve updated content
        if (builder == null) {
            builder = Response.ok(result);
        }
        builder.tag(tag);
        CacheControl cc = getCacheControl();
        builder.cacheControl(cc);
        logger.info("filter() : " + result);
        return builder.build();
    }

}
