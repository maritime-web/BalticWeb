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
        List<Forecast> found = forecastDao.findByProviderAreaAndType(forecast.getProvider(), forecast.getArea(), forecast.getType());
        if (found == null || found.isEmpty()) {
            forecastDao.saveEntity(forecast);
        } else {
            Forecast latest = found.get(0);
            if (latest.getTimestamp() < forecast.getTimestamp()) {
                latest.invalidate();
                forecastDao.saveEntity(forecast);
                forecastDao.saveEntity(latest);
            } else if (latest.getTimestamp() == forecast.getTimestamp() && forecast.getData().length() > latest.getData().length()) {
                // If we have a forecast from two different files, we keep the bigger one.
                latest.updateData(forecast.getData(), forecast.getSize());
                forecastDao.saveEntity(latest);
            }
            if (found.size() > 1) {
                for (int i = 1; i < found.size(); i++) {
                    found.get(i).invalidate();
                    forecastDao.saveEntity(found.get(i));
                }
            }

        }
    }

}
