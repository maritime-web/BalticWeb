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

package dk.dma.embryo.tiles.service;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.persistence.DaoImpl;
import dk.dma.embryo.tiles.model.TileSet;

/**
 * Created by Jesper Tejlgaard on 8/26/14.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class TileSetDaoImpl extends DaoImpl implements TileSetDao {

    @Inject
    private EntityManager em;

    @Inject
    @Property("embryo.tiles.baseUrl")
    private String baseUrl;

    private List<TileSet> setBaseUrl(List<TileSet> tileSets) {
        for (TileSet tileSet : tileSets) {
            tileSet.setUrl(baseUrl + tileSet.getName());
        }
        return tileSets;
    }

    public List<TileSet> listByStatus(TileSet.Status status) {
        TypedQuery<TileSet> tq = em.createNamedQuery("TileSet:listByStatus", TileSet.class);
        tq.setParameter("status", status);
        return setBaseUrl(tq.getResultList());
    }

    public List<TileSet> listByProviderAndType(String provider, String type) {
        TypedQuery<TileSet> tq = em.createNamedQuery("TileSet:listByProviderAndType", TileSet.class);
        tq.setParameter("provider", provider);
        tq.setParameter("tp", type);
        return setBaseUrl(tq.getResultList());
    }

    public List<TileSet> listByTypeAndStatus(String provider, TileSet.Status status) {
        TypedQuery<TileSet> tq = em.createNamedQuery("TileSet:listByTypeAndStatus", TileSet.class);
        tq.setParameter("tp", provider);
        tq.setParameter("status", status);
        return setBaseUrl(tq.getResultList());
    }

    public List<TileSet> listByProviderAndTypeAndStatus(String provider, String type, TileSet.Status status) {
        TypedQuery<TileSet> tq = em.createNamedQuery("TileSet:listByProviderAndTypeAndStatus", TileSet.class);
        tq.setParameter("provider", provider);
        tq.setParameter("tp", type);
        tq.setParameter("status", status);
        return setBaseUrl(tq.getResultList());
    }

    public TileSet getByNameAndProvider(String name, String provider) {
        TypedQuery<TileSet> tq = em.createNamedQuery("TileSet:getByNameAndProvider", TileSet.class);
        tq.setParameter("name", name);
        tq.setParameter("provider", provider);
        return getSingleOrNull(tq.getResultList());
    }
}
