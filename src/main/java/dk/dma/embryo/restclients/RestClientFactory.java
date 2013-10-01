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
package dk.dma.embryo.restclients;


import dk.dma.configuration.Property;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.client.ProxyFactory;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

@Singleton
public class RestClientFactory {
    @Inject
    @Property("dk.dma.embryo.restclients.dmiSejlRuteServiceUrl")
    private String dmiSejlRuteServiceUrl;

    @Inject
    @Property("dk.dma.embryo.restclients.aisViewServiceUrl")
    private String aisViewServiceUrl;

    @Produces
    public DmiSejlRuteService createSejlRuteService() {
        return ProxyFactory.create(DmiSejlRuteService.class, dmiSejlRuteServiceUrl);
    }

    @Produces
    public AisViewService createAisViewService() {
        return ProxyFactory.create(AisViewService.class, aisViewServiceUrl);
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
