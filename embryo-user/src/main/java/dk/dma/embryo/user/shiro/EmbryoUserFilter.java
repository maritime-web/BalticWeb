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
import java.io.PrintWriter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.web.filter.authc.UserFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

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
