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
package dk.dma.embryo.vessel.json.client;

import java.io.IOException;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.client.ProxyFactory;

import dk.dma.embryo.common.configuration.Property;

@Singleton
public class AisJsonClientFactory {
    @Inject
    @Property("dk.dma.embryo.restclients.dmiSejlRuteServiceUrl")
    private String dmiSejlRuteServiceUrl;

    @Inject
    @Property("dk.dma.embryo.restclients.limitedAisViewServiceUrl")
    private String limitedAisViewServiceUrl;

    @Inject
    @Property("dk.dma.embryo.restclients.fullAisViewServiceUrl")
    private String fullAisViewServiceUrl;

     @Produces
    public LimitedAisViewService createLimitedAisViewService() {
        return ProxyFactory.create(LimitedAisViewService.class, limitedAisViewServiceUrl);
    }

    @Produces
    public FullAisViewService createFullAisViewService() {
        return ProxyFactory.create(FullAisViewService.class, fullAisViewServiceUrl);
    }

    public static String asJson(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
