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
package dk.dma.embryo.vessel.persistence;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import dk.dma.embryo.common.persistence.DaoImpl;
import dk.dma.embryo.vessel.model.Berth;

@Stateless
public class GeographicDaoImpl extends DaoImpl implements GeographicDao {

    public GeographicDaoImpl() {
        super();
    }

    public GeographicDaoImpl(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public List<Berth> findBerths(String queryString) {
        TypedQuery<Berth> query = em.createNamedQuery("Berth:findByQuery", Berth.class);
        query.setParameter("query", queryString + "%");
        List<Berth> result = query.getResultList();
        return result;
    }

    @Override
    public List<Berth> lookup(String name) {
        TypedQuery<Berth> query = em.createNamedQuery("Berth:lookup", Berth.class);
        query.setParameter("name", name != null ? name.toUpperCase() : name);
        List<Berth> result = query.getResultList();
        return result;
    }
}
