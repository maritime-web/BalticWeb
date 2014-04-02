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
package dk.dma.embryo.msi;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.log.EmbryoLogService;
import dk.frv.enav.msi.ws.warning.MsiService;
import dk.frv.enav.msi.ws.warning.WarningService;
import dk.frv.msiedit.core.webservice.message.MsiDto;

public class MsiClientImpl implements MsiClient {
    @Inject
    @Property("embryo.msi.endpoint")
    String endpoint;

    @Inject
    @Property("embryo.msi.countries")
    String countries;

    @Inject
    private EmbryoLogService embryoLogService;

    private MsiService msiService;

    @PostConstruct
    public void init() {
        msiService = new WarningService(getClass().getResource("/wsdl/warning.wsdl"), new QName("http://enav.frv.dk/msi/ws/warning", "WarningService"))
                .getMsiServiceBeanPort();

        ((BindingProvider) msiService).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
    }

    public List<MsiClient.MsiItem> getActiveWarnings() {
        try {
            List<MsiClient.MsiItem> result = new ArrayList<>();

            String[] countriesArr = countries.split(",");

            for (String country : countriesArr) {
                for (MsiDto md : msiService.getActiveWarningCountry(country).getItem()) {
                    result.add(new MsiClient.MsiItem(md));
                }
            }

            embryoLogService.info("Read " + result.size() + " warnings from MSI service: " + endpoint);
            return result;

        } catch (Throwable t) {
            embryoLogService.error("Error reading warnings from MSI service: " + endpoint, t);

            throw new RuntimeException(t);
        }
    }
}
