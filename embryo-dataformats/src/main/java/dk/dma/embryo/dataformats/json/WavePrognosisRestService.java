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
package dk.dma.embryo.dataformats.json;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import dk.dma.embryo.dataformats.netcdf.NetCDFResult;
import dk.dma.embryo.dataformats.service.WavePrognosisService;

@Path("/waves")
public class WavePrognosisRestService {
    @Inject
    private WavePrognosisService wavePrognosisService;

    @Inject
    private Logger logger;

    @GET
    @Path("/prognoses")
    @Produces("application/json")
    @GZIP
    @NoCache
    public List<String> listWavePrognoses() {
        logger.debug("listWavePrognoses()");
        return wavePrognosisService.listAvailableWavePrognoses();
    }

    @GET
    @Path("/prognoses/{id}")
    @Produces("application/json")
    @GZIP
    @NoCache
    public NetCDFResult getWavePrognosis(@PathParam(value = "id") String id) {
        logger.debug("getWavePrognosis()");

        return wavePrognosisService.getPrognosis(id);
    }

}
