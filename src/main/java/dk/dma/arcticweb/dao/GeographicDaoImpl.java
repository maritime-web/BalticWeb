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
package dk.dma.arcticweb.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import dk.dma.embryo.domain.Berth;

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
}
