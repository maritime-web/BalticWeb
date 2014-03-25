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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJBTransactionRolledbackException;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;

import dk.dma.embryo.domain.FormatException;

/**
 * @author Jesper Tejlgaard
 */
@WebFilter(filterName = "ExceptionFilter", urlPatterns = { "/rest/*"})
public class ExceptionFilter implements Filter {

    @Inject
    private Logger logger;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        try {
            chain.doFilter(request, response);
        } catch (RuntimeException e) {
            logger.info("Filtering exception " + e, e);
            Throwable t = e;
            if (t instanceof WebApplicationException) {
                // should never end here, becaue REST EASY handling WebApplicationException closer to RS services
                throw e;
            }

            // Can unfortunately not avoid this, RestEasy wrappes all exceptions
            if (t instanceof org.jboss.resteasy.spi.UnhandledException) {
                t = e.getCause();
            }

            setStatus(response, t);
            response.setContentType("application/json");
            writeException(response, t);
        }
    }

    private void writeException(ServletResponse response, Throwable t) throws IOException {
        if (t instanceof EJBTransactionRolledbackException) {
            t = getCause(t);
        }

        PrintWriter writer = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(buildJson(t));
            writer.write(json);
        } catch (IOException e2) {
            throw new RuntimeException(e2);
        }

        writer.close();

    }

    Object buildJson(Throwable t) {
        if (t instanceof ConstraintViolationException) {
            return buildMessage((ConstraintViolationException) t);
        }

        return new String[] { t.getMessage() };
    }

    private void setStatus(ServletResponse response, Throwable e) throws IOException {
        if (e instanceof EJBTransactionRolledbackException) {
            e = getCause(e);
        }

        if (e instanceof AuthenticationException) {
            ((HttpServletResponse) response).setStatus(Status.UNAUTHORIZED.getStatusCode());
        }else if (e instanceof AuthorizationException) {
            ((HttpServletResponse) response).setStatus(Status.FORBIDDEN.getStatusCode());
        } else if (e instanceof ConstraintViolationException) {
            ((HttpServletResponse) response).setStatus(Status.BAD_REQUEST.getStatusCode());
        } else if (e instanceof IllegalArgumentException) {
            ((HttpServletResponse) response).setStatus(Status.BAD_REQUEST.getStatusCode());
        } else if (e instanceof FormatException) {
            ((HttpServletResponse) response).setStatus(Status.BAD_REQUEST.getStatusCode());
        } else {
            ((HttpServletResponse) response).setStatus(Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
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

    private static Throwable getCause(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }

        return t;
    }

    @Override
    public void destroy() {
    }

}
