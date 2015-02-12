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
package dk.dma.embryo.dataformats.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.dataformats.model.IceObservation;
import dk.dma.embryo.dataformats.model.Provider;
import dk.dma.embryo.dataformats.model.ShapeFileMeasurement;
import dk.dma.embryo.dataformats.persistence.ShapeFileMeasurementDao;
import dk.dma.embryo.dataformats.transform.Shape2IceListTransformer;
import dk.dma.embryo.dataformats.transform.Shape2IceTransformerFactory;

@Stateless
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class IceObservationServiceImpl implements IceObservationService {

    @Inject
    private ShapeFileMeasurementDao shapeFileMeasurementDao;

    @Property(value = "embryo.iceChart.providers")
    @Inject
    private Map<String, String> providers;
    
    @Inject
    private Shape2IceTransformerFactory transformerFactory;

    @Inject
    private PropertyFileService propertyService;

    
    @Override
    public List<Provider> listIceChartProviders() {
        List<Provider> result = new ArrayList<>(providers.size());
        
        for(Entry<String, String> prov : providers.entrySet()){
            String name = propertyService.getProperty("embryo.iceChart." + prov.getKey() + ".name");
            result.add(new Provider(prov.getKey(), name, prov.getValue()));
        }
        
        return result;
    }

    public List<IceObservation> listAvailableIceObservations(String chartType) {
        List<ShapeFileMeasurement> shapeMeasurements = shapeFileMeasurementDao.list(chartType);
        Shape2IceListTransformer transformer = transformerFactory.createListTransformer();
        return transformer.transform(shapeMeasurements);
    }
}
