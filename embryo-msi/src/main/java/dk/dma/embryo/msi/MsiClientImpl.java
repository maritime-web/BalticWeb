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
