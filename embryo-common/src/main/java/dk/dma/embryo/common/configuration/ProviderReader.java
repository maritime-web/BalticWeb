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

package dk.dma.embryo.common.configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jesper Tejlgaard on 10/2/14.
 */
public class ProviderReader {

    private PropertyFileService propertyFileService;

    public ProviderReader(PropertyFileService propertyFileService) {
        this.propertyFileService = propertyFileService;
    }

    public List<Type> readTypeProperties(String typesProperty) {
        List<String> types = propertyFileService.getListProperty(typesProperty);

        List<Type> result = new ArrayList<>(types.size());
        for (String typeKey : types) {
            String baseProp = typesProperty + "." + typeKey + ".";
            String localDirectory = propertyFileService.getProperty(baseProp + "localDirectory", true);
            String name = propertyFileService.getProperty(baseProp + "name");

            Type type = new Type(name == null ? typeKey : name, localDirectory);
            result.add(type);
        }

        return result;
    }

    public List<Provider> readProviderProperties(String providersProperty) {
        List<String> providers = propertyFileService.getListProperty(providersProperty);

        List<Provider> result = new ArrayList<>(providers.size());
        for (String providerKey : providers) {
            String providerProp = providersProperty + "." + providerKey + ".";
            String shortName = propertyFileService.getProperty(providerProp + "shortName");
            String notificationEmail = propertyFileService.getProperty(providerProp + "notification.email");

            Provider provider = new Provider(shortName == null ? providerKey : shortName, notificationEmail);
            provider.setTypes(readTypeProperties(providerProp + "types"));
            result.add(provider);
        }
        return result;
    }
}
