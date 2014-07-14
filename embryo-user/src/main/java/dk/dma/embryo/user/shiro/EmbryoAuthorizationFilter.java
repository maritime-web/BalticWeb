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

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.embryo.user.shiro.Error.AuthCode;

/**
 * @author Jesper Tejlgaard
 */
public class EmbryoAuthorizationFilter extends AuthorizationFilter {

    Logger logger = LoggerFactory.getLogger(EmbryoAuthorizationFilter.class);

    
    @Override
    public boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws IOException {
        Subject subject = getSubject(request, response);
        String[] rolesArray = (String[]) mappedValue;

        if (rolesArray == null || rolesArray.length == 0) {
            //no roles specified, so nothing to check - allow access.
            return true;
        }

        for(String role : rolesArray){
            if(subject.hasRole(role)){
                return true;
            }
        }
        
        return false;
    }

    
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response, Object mappedValue)
            throws IOException {
        logger.debug("Access denied: {}, {}", WebUtils.toHttp(request).getRequestURI(), mappedValue);

        Subject subject = getSubject(request, response);

        HttpServletResponse httpResp = WebUtils.toHttp(response);
        httpResp.setContentType("application/json");
        if (subject.getPrincipal() == null) {
            httpResp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            Util.writeJson(httpResp, new Error(AuthCode.UNAUTHENTICATED, "User not logged in"));
        } else {
            httpResp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            Util.writeJson(httpResp, new Error(AuthCode.UNAUTHORIZED,
                    "User is logged in, but does not have necessary permissions"));
        }
        return false;
    }
}
