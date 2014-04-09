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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.embryo.user.shiro.Error.AuthCode;

/**
 * @author Jesper Tejlgaard
 */
public abstract class EmbryoVesselDataFilter extends AccessControlFilter {

    Logger logger = LoggerFactory.getLogger(EmbryoVesselDataFilter.class);

    @Override
    public void doFilterInternal(ServletRequest request, ServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        MultiReadHttpServletRequest multiReadRequest = new MultiReadHttpServletRequest((HttpServletRequest) request);
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
