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
package dk.dma.embryo.dataformats.persistence;

import java.util.List;

import javax.persistence.TypedQuery;

import dk.dma.embryo.common.persistence.DaoImpl;
import dk.dma.embryo.dataformats.model.Forecast;
import dk.dma.embryo.dataformats.model.Forecast.Provider;
import dk.dma.embryo.dataformats.model.ForecastType.Type;

public class ForecastDaoImpl extends DaoImpl implements ForecastDao {

    @Override
    public Forecast findByNameAndType(String name, Type type) {
        TypedQuery<Forecast> query = em.createNamedQuery("Forecast:lookup", Forecast.class);
        query.setParameter("name", name);
        query.setParameter("type", type);
        List<Forecast> resultList = query.getResultList();
        if (resultList != null && !resultList.isEmpty()) {
            return resultList.get(0);
        }
        return null;
    }
    
    @Override
    public List<Forecast> findByProviderAreaAndType(Provider provider, String area, Type type) {
        TypedQuery<Forecast> query = em.createNamedQuery("Forecast:findDuplicates", Forecast.class);
        query.setParameter("provider", provider);
        query.setParameter("area", area);
        query.setParameter("type", type);
        List<Forecast> resultList = query.getResultList();
        return resultList;
    }
    
    @Override
    public Forecast findById(long id) {
        TypedQuery<Forecast> query = em.createNamedQuery("Forecast:find", Forecast.class);
        query.setParameter("id", id);
        List<Forecast> resultList = query.getResultList();
        if (resultList != null && !resultList.isEmpty()) {
            return resultList.get(0);
        }
        return null;
    }

    @Override
    public List<Forecast> list(Type type) {
        TypedQuery<Forecast> query = em.createNamedQuery("Forecast:list", Forecast.class);
        query.setParameter("type", type);
        return query.getResultList();
    }

    @Override
    public boolean exists(Provider provider, long timestamp) {
        TypedQuery<Long> query = em.createNamedQuery("Forecast:exists", Long.class);
        query.setParameter("provider", provider);
        query.setParameter("timestamp", timestamp);
        Long count = query.getSingleResult();
        return count > 0;
    }

    @Override
    public void flush() {
        em.flush();
    }

}
