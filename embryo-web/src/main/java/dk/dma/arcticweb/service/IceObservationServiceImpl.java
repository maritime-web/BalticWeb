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
package dk.dma.arcticweb.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import dk.dma.arcticweb.component.ice.Shape2IceTransformer;
import dk.dma.arcticweb.component.ice.Shape2IceTransformerFactory;
import dk.dma.arcticweb.dao.ShapeFileMeasurementDao;
import dk.dma.embryo.configuration.Property;
import dk.dma.embryo.configuration.PropertyFileService;
import dk.dma.embryo.domain.IceObservation;
import dk.dma.embryo.domain.Provider;
import dk.dma.embryo.domain.ShapeFileMeasurement;
import dk.dma.embryo.security.AuthorizationChecker;
import dk.dma.embryo.security.authorization.RolesAllowAll;

@Stateless
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
@Interceptors(value = AuthorizationChecker.class)
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
    @RolesAllowAll
    public List<Provider> listIceChartProviders() {
        List<Provider> result = new ArrayList<>(providers.size());
        
        for(Entry<String, String> prov : providers.entrySet()){
            String name = propertyService.getProperty("embryo.iceChart." + prov.getKey() + ".name");
            result.add(new Provider(prov.getKey(), name, prov.getValue()));
        }
        
        return result;
    }

    @RolesAllowAll
    public List<IceObservation> listAvailableIceObservations(String provider) {
        List<ShapeFileMeasurement> shapeMeasurements = shapeFileMeasurementDao.list(provider);
        Shape2IceTransformer transformer = transformerFactory.createTransformer(provider);
        return transformer.transform(shapeMeasurements);
    }
}
