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
