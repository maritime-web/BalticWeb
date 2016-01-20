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

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import dk.dma.embryo.dataformats.model.Forecast;
import dk.dma.embryo.dataformats.persistence.ForecastDao;

@Stateless
public class ForecastPersistServiceImpl implements ForecastPersistService {

    @Inject
    private ForecastDao forecastDao;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    @Override
    public void persist(Forecast forecast) {

        int size = forecast.getSize();
        List<Forecast> found = forecastDao.findByProviderAreaAndType(forecast.getProvider(), forecast.getArea(), forecast.getType());
        boolean saved = false;
        if(found != null && !found.isEmpty()) {
            // We have existing entries for this provider, area and type
            for(Forecast f : found) {
                if(f.getTimestamp() > forecast.getTimestamp()) {
                    // Forecast is newer than the current
                    if(f.getSize() != -1 && size != -1) {
                        // The newer forecast has data
                        forecast.invalidate();
                        size = -1;
                    }
                } else if(f.getTimestamp() == forecast.getTimestamp()) {
                    // Same forecast
                    if(size > f.getSize()) {
                        // This forecast is bigger
                        if (f.getName().equals(forecast.getName())) {
                            // Same name - we update data
                            f.updateData(forecast.getForecastDataId(), size);
                            saved = true;
                        } else {
                            // Different name - old goes out
                            f.invalidate();
                        }
                        forecastDao.saveEntity(f);
                    } else {
                        if(f.getName().equals(forecast.getName())) {
                            // Same name - do not process this forecast further
                            saved = true;
                        } else {
                            // Different name - old stays
                            forecast.invalidate();
                        }
                    }
                } else {
                    // Current forecast is newer
                    if(size != -1 && f.getSize() != -1) {
                        f.invalidate();
                        forecastDao.saveEntity(f);
                    }
                }
            }
        }
        if(!saved) {
            forecastDao.saveEntity(forecast);
        }
    }
}
