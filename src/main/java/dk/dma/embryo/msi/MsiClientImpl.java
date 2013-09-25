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

import dk.dma.configuration.Property;
import dk.frv.enav.msi.ws.warning.MsiService;
import dk.frv.enav.msi.ws.warning.WarningService;
import dk.frv.msiedit.core.webservice.message.MsiDtoLight;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import java.util.ArrayList;
import java.util.List;

public class MsiClientImpl implements MsiClient {
    @Inject
    @Property("embryo.msi.endpoint")
    String endpoint;

    private MsiService msiService;

    @PostConstruct
    public void init() {
        msiService = new WarningService(getClass().getResource("/wsdl/warning.wsdl"),
                new QName("http://enav.frv.dk/msi/ws/warning", "WarningService")).getMsiServiceBeanPort();

        ((BindingProvider) msiService).getRequestContext().put(
                BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint
        );
    }

    public List<MsiItem> getActiveWarnings() {
        List<MsiItem> result = new ArrayList<>();

        for (MsiDtoLight mdl : msiService.getActiveWarning().getItem()) {
            result.add(new MsiItem(mdl));
        }

        return result;
    }
}
