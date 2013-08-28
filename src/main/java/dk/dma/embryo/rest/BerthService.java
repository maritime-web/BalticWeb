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

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.slf4j.Logger;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import dk.dma.arcticweb.service.GeographicService;
import dk.dma.arcticweb.service.ShipService;
import dk.dma.embryo.domain.Berth;
import dk.dma.embryo.domain.Voyage;
import dk.dma.embryo.rest.util.DateTimeConverter;
import dk.dma.embryo.rest.util.TypeaheadDatum;
import dk.dma.embryo.site.form.VoyagePlanForm.BerthDatum;
import dk.dma.embryo.site.form.VoyagePlanForm.BerthTransformerFunction;

/**
 * 
 * @author Jesper Tejlgaard
 */
@Path("/berth")
public class BerthService {

    @Inject
    private GeographicService geoService;

    @Inject
    private Logger logger;

    public BerthService() {
    }
    
//    @GET
//    @Path("/list/{mmsi}")
//    @Produces("application/json")
//    public List<BerthDatum> remote(String query) {
//        logger.trace("remoteFetch({})", query);
//
//        List<Berth> berths = geoService.findBerths(query);
//        List<BerthDatum> transformed = Lists.transform(berths, new BerthTransformerFunction());
//
//        logger.trace("berths={}", transformed);
//
//        return transformed;
//    }
//
//    @GET
//    @Path("/remote/{mmsi}")
//    @Produces("application/json")
//    @Override
//    public List<BerthDatum> prefetch() {
//        logger.trace("prefetch()");
//
//        List<Berth> berths = geoService.findBerths("");
//        List<BerthDatum> transformed = Lists.transform(berths, new BerthTransformerFunction());
//
//        logger.trace("berths={}", transformed);
//
//        return transformed;
//    }

}
