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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.embryo.common.configuration.Configuration;
import dk.dma.embryo.user.security.Subject;
import dk.dma.embryo.user.shiro.Error.AuthCode;

/**
 * @author Jesper Tejlgaard
 */
public class EmbryoSailorOnVesselFilter extends EmbryoVesselDataFilter {

    Logger logger = LoggerFactory.getLogger(EmbryoSailorOnVesselFilter.class);

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue)
            throws Exception {

        Subject subject = Configuration.getBean(Subject.class);
        Object jsonObject = Util.getJson(request.getInputStream(), Object.class);
        Number mmsi = Util.getValue(jsonObject, ((String[]) mappedValue)[0]);

        return subject.authorizedToModifyVessel(mmsi.longValue());
    }


}
