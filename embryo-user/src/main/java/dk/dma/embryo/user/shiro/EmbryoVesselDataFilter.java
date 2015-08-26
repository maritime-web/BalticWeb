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

import dk.dma.embryo.common.servlet.MultiReadHttpServletRequest;
import dk.dma.embryo.user.shiro.Error.AuthCode;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Jesper Tejlgaard
 */
public abstract class EmbryoVesselDataFilter extends AccessControlFilter {

    Logger logger = LoggerFactory.getLogger(EmbryoVesselDataFilter.class);

    @Override
    public void doFilterInternal(ServletRequest request, ServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        MultiReadHttpServletRequest multiReadRequest = MultiReadHttpServletRequest.create((HttpServletRequest) request);
        super.doFilterInternal(multiReadRequest, response, chain);
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response, Object mappedValue)
            throws IOException {
        logger.debug("Access denied: {}, {}", WebUtils.toHttp(request).getRequestURI(), ((String[]) mappedValue)[0]);

        HttpServletResponse httpResp = WebUtils.toHttp(response);
        httpResp.setContentType("application/json");
        httpResp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        Util.writeJson(httpResp, new Error(AuthCode.UNAUTHORIZED,
                "User not authorized to modify data for vessel in question"));
        return false;
    }
    

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        // never invoked
        return false;
    }


}
