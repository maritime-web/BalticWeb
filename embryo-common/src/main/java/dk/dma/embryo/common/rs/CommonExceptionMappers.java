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
package dk.dma.embryo.common.rs;

import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJBTransactionRolledbackException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import dk.dma.embryo.common.util.FormatException;

/**
 * @author Jesper Tejlgaard
 */
@Provider
public class CommonExceptionMappers {

    public static Throwable getCause(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }

    @Provider
    public static class EJBTransactionRolledbackMapper implements ExceptionMapper<EJBTransactionRolledbackException> {
        @Override
        public Response toResponse(EJBTransactionRolledbackException exception) {
            Throwable t = exception;
            t = getCause(t);

            Object message = t.getMessage();
            if (t instanceof ConstraintViolationException) {
                return new ConstraintViolationMapper().toResponse((ConstraintViolationException) t);
            } else if (t instanceof IllegalArgumentException) {
                return new IllegalArgumentMapper().toResponse((IllegalArgumentException) t);
            } else if (t instanceof FormatException) {
                return new FormatMapper().toResponse((FormatException) t);
            }

            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(message).build();
        }
    }

    @Provider
    public static class ConstraintViolationMapper implements ExceptionMapper<ConstraintViolationException> {
        @Override
        public Response toResponse(ConstraintViolationException exception) {
            return Response.status(Status.BAD_REQUEST).entity(buildMessage(exception)).build();
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
    }

    @Provider
    public static class IllegalArgumentMapper implements ExceptionMapper<IllegalArgumentException> {
        @Override
        public Response toResponse(IllegalArgumentException exception) {
            return Response.status(Status.BAD_REQUEST).entity(exception.getMessage()).build();
        }
    }

    @Provider
    public static class FormatMapper implements ExceptionMapper<FormatException> {
        @Override
        public Response toResponse(FormatException exception) {
            return Response.status(Status.BAD_REQUEST).entity(exception.getMessage()).build();
        }
    }
    
    public static Class<?>[] getMappers(){
        return CommonExceptionMappers.class.getClasses();
    }
}
