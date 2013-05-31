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

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;

import dk.dma.arcticweb.domain.IEntity;
import dk.dma.arcticweb.domain.authorization.SecuredUser;

public class DaoImpl implements Dao {

    @PersistenceContext(name = "arcticweb")
    protected EntityManager em;

    protected DaoImpl(EntityManager entityManager) {
        this.em = entityManager;
    }

    @Override
    public IEntity getByPrimaryKey(Class<? extends IEntity> clazz, Object id) {
        try {
            return em.find(clazz, id);
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    @Override
    public void remove(IEntity entity) {
        em.remove(em.merge(entity));
    }

    @Override
    public IEntity saveEntity(IEntity entity) {
        if (entity.isPersisted()) {
            // Update existing
            entity = em.merge(entity);
        } else {
            // Save new
            em.persist(entity);
        }
        return entity;
    }

    public static IEntity getSingleOrNull(List<? extends IEntity> list) {
        return (list == null || list.size() == 0) ? null : list.get(0);
    }
}
