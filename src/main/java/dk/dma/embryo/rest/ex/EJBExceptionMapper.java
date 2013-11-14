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

import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.EJBTransactionRolledbackException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class EJBExceptionMapper implements ExceptionMapper<EJBException> {

    @Override
    public Response toResponse(EJBException exception) {
        Throwable ex = exception;
        if (ex instanceof EJBTransactionRolledbackException) {
            ex = getCause(exception);
        }

        if (ex instanceof ConstraintViolationException) {
            return Response.status(Status.BAD_REQUEST).entity(buildMessage((ConstraintViolationException) ex))
                    .type(MediaType.APPLICATION_JSON).build();
        }

        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).type(MediaType.TEXT_PLAIN).build();
    }

    private static List<String> buildMessage(ConstraintViolationException ex) {
        List<String> result = new LinkedList<>();
        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            StringBuilder builder = new StringBuilder();
            builder.append("Invalid value '").append(v.getInvalidValue()).append("'. ")
                    .append(v.getPropertyPath().toString()).append(" ").append(v.getMessage());
            result.add(builder.toString());
        }
        return result;
    }

    // static <CT extends Exception> CT getCause(Exception e, Class<? extends Exception> causeType){
    //
    // }

    private static Throwable getCause(Exception e) {
        Throwable t = e;
        while (t.getCause() != null) {
            t = t.getCause();
        }

        return t;
    }
}
