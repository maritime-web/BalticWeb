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
package dk.dma.embryo.common.servlet;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.StringWriter;

public class DynamicResourceLoggingFilter implements Filter {

    private final Logger RESOURCE_LOGGER = LoggerFactory.getLogger("dk.dma.embryo.dynamicResource");

    public void init(FilterConfig filterConfig) throws ServletException {

    }

    public void doFilter(ServletRequest req, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        if (isMultiReadHttpServletRequestNecessary(request)) {
            request = MultiReadHttpServletRequest.create(request);
        }

        traceRequest(RESOURCE_LOGGER, request);
        chain.doFilter(request, response);
    }

    private static boolean isMultiReadHttpServletRequestNecessary(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod()) || "PUT".equalsIgnoreCase(request.getMethod());
    }

    public void destroy() {

    }

    public static void traceRequest(Logger logger, ServletRequest req) {
        HttpServletRequest request = (HttpServletRequest) req;

        if (logger.isTraceEnabled()) {
            // Do not log zabbix calls
            if ("/rest/log/latest".equals(request.getRequestURI())) {
                return;
            }

            String requestStr = request.getRequestURI();

            if (request.getQueryString() != null && request.getQueryString().trim().length() > 0) {
                requestStr += "?" + request.getQueryString();
            }

            String body = "";
            if (isMultiReadHttpServletRequestNecessary(request)) {
                try {
                    StringWriter writer = new StringWriter();
                    request.getInputStream();
                    IOUtils.copy(request.getInputStream(), writer, "UTF-8");
                    body = writer.toString();
                } catch (IOException e) {
                    e.printStackTrace();

                }
                //TODO read body containing json
                //multireadrequest necessary
            }
            logger.trace("{} {} {}", request.getMethod(), requestStr, body);
        }
    }
}
