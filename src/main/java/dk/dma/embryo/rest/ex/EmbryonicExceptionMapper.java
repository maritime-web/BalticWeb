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
package dk.dma.embryo.rest.ex;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import dk.dma.embryo.EmbryonicException;
import dk.dma.embryo.domain.FormatException;

@Provider
public class EmbryonicExceptionMapper implements ExceptionMapper<EmbryonicException> {

    @Override
    public Response toResponse(EmbryonicException ex) {

        System.out.println("EXCEPTION MAPPER");
        ex.printStackTrace();
        
        if (ex instanceof FormatException) {
            return Response.status(Status.BAD_REQUEST).entity(new String[] { ex.getMessage() })
                    .type(MediaType.APPLICATION_JSON).build();
        }

        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).type(MediaType.TEXT_PLAIN).build();
    }
}
