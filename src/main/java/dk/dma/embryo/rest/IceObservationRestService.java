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

import dk.dma.arcticweb.service.IceObservationService;
import dk.dma.embryo.domain.IceObservation;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;

import java.util.List;

@Path("/ice")
public class IceObservationRestService {
    @Inject
    private IceObservationService iceObservationService;

    @GET
    @Path("/list")
    @Produces("application/json")
    @GZIP
    @NoCache
    public List<IceObservation> listIceObservations() {
        return iceObservationService.listAvailableIceObservations();
    }
}
