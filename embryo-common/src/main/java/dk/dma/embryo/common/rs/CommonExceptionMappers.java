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
package dk.dma.embryo.common.rs;

import dk.dma.embryo.common.util.FormatException;

import javax.ejb.EJBTransactionRolledbackException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedList;
import java.util.List;

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

    public static Class<?>[] getMappers(){
        return CommonExceptionMappers.class.getClasses();
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
            return Response.status(Status.BAD_REQUEST).entity(new String[]{exception.getMessage()}).build();
        }
    }

    @Provider
    public static class FormatMapper implements ExceptionMapper<FormatException> {
        @Override
        public Response toResponse(FormatException exception) {
            return Response.status(Status.BAD_REQUEST).entity(new String[]{exception.getMessage()}).build();
        }
    }
}
