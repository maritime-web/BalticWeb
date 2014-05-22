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
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.common.log.EmbryoLogService;
import dk.frv.enav.msi.ws.warning.MsiService;
import dk.frv.enav.msi.ws.warning.WarningService;
import dk.frv.msiedit.core.webservice.message.MsiDto;

public class MsiClientImpl implements MsiClient {
    @Inject
    @Property("embryo.msi.endpoint")
    String endpoint;

    @Inject
    @Property("embryo.msi.regions")
    String regions;

    @Inject
    private EmbryoLogService embryoLogService;
    
    @Inject
    private PropertyFileService propertyFileService;

    private MsiService msiService;

    private Map<String, String> descriptions;
    
    public MsiClientImpl(){
    }

    public MsiClientImpl(EmbryoLogService logService){
        this.embryoLogService = logService;
    }

    
    @PostConstruct
    public void init() {
        descriptions = getDescriptions();
        msiService = new WarningService(getClass().getResource("/wsdl/warning.wsdl"), new QName("http://enav.frv.dk/msi/ws/warning", "WarningService"))
                .getMsiServiceBeanPort();

        ((BindingProvider) msiService).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
    }

    public List<MsiClient.MsiItem> getActiveWarnings(List<String> regions) {
        List<MsiClient.MsiItem> result = new ArrayList<>();

        boolean error = false;
        for (String region : regions) {
            int count = 0;
            try {
                for (MsiDto md : msiService.getActiveWarningCountry(region).getItem()) {
                    result.add(new MsiClient.MsiItem(md));
                    count++;
                }
            } catch (Throwable t) {
                embryoLogService.error("Error reading warnings from MSI service: " + endpoint + " for country: " + region, t);
                error = true;
            }
            embryoLogService.info("Read " + count + " warnings from MSI provider: " + region);
        }
        embryoLogService.info("Read " + result.size() + " warnings from MSI service: " + endpoint);
        if(error) {
            embryoLogService.error("There was a problem reading MSI warnings from endpoint " + endpoint);
            throw new RuntimeException();
        }
        return result;
    }
    
    @Override
    public List<Region> getRegions() {
        String[] regionStrs = regions.split(",");
        ArrayList<Region> result = new ArrayList<>();
        for(String r : regionStrs) {
            Region region = new Region();
            region.setName(r);
            region.setDescription(descriptions.get(r));
            result.add(region);
        }
        return result;
    }
    
    private Map<String, String> getDescriptions() {
        Map<String, String> descriptions = propertyFileService.getMapProperty("embryo.msi.region.descriptions");
        return descriptions;
    }
}
