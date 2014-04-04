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
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.web.filter.authc.UserFilter;
import org.apache.shiro.web.util.WebUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.embryo.user.shiro.Error.AuthCode;

/**
 * @author Jesper Tejlgaard
 */
public class EmbryoUserFilter extends UserFilter {

    Logger logger = LoggerFactory.getLogger(EmbryoUserFilter.class);

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response)
            throws IOException {
        logger.debug("Access denied: {}", WebUtils.toHttp(request).getRequestURI());

        HttpServletResponse httpResp = WebUtils.toHttp(response);
        httpResp.setContentType("application/json");
        httpResp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        writeJson(httpResp, new Error(AuthCode.UNAUTHENTICATED, "User not logged in"));
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
