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
package dk.dma.embryo.user.shiro;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.RolesAuthorizationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.embryo.user.shiro.Error.AuthCode;

/**
 * @author Jesper Tejlgaard
 */
public class EmbryoAuthorizationFilter extends RolesAuthorizationFilter {

    Logger logger = LoggerFactory.getLogger(EmbryoAuthorizationFilter.class);
    
    public boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws IOException {
        boolean allowed = super.isAccessAllowed(request, response, mappedValue);
        if(!allowed){
            logger.info("Denied access to {} - {}", url(WebUtils.toHttp(request)), mappedValue);
        }
        return allowed;
    }
    
    String url(HttpServletRequest request){
        logger.debug(request.getContextPath());
        logger.debug(request.getPathInfo());
        logger.debug(request.getRequestURI());
        logger.debug(request.getServletPath());
        
        return request.getRequestURI();
    }
    
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws IOException {
        Subject subject = getSubject(request, response);
        // If the subject isn't identified, redirect to login URL

        HttpServletResponse httpResp = WebUtils.toHttp(response);
        httpResp.setContentType("application/json");

        if (subject.getPrincipal() == null) {
            httpResp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(httpResp, new Error(AuthCode.UNAUTHENTICATED, "User not logged in"));
        } else {
            httpResp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(httpResp, new Error(AuthCode.UNAUTHORIZED,
                    "User is logged in, but does not have necessary permissions"));
        }
        return false;
    }

    private void writeJson(HttpServletResponse response, Object object) throws IOException {
        PrintWriter writer = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(object);
            writer.write(json);
        } catch (IOException e2) {
            throw new RuntimeException(e2);
        }

        writer.close();
    }

}
