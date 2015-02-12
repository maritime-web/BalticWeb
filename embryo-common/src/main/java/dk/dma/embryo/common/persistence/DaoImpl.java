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
package dk.dma.embryo.common.persistence;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * The Class DaoImpl.
 */
public abstract class DaoImpl implements Dao {

    /** The Entity Manager. */
    @Inject
    protected EntityManager em;

    /**
     * Instantiates a new dao impl.
     */
    protected DaoImpl() {
    }

    /**
     * Instantiates a new dao impl.
     *
     * @param entityManager the entity manager
     */
    protected DaoImpl(EntityManager entityManager) {
        this.em = entityManager;
    }

    /* (non-Javadoc)
     * @see dk.dma.embryo.common.persistence.Dao#getByPrimaryKey(java.lang.Class, java.lang.Object)
     */
    @Override
    public <E extends IEntity<?>> E getByPrimaryKey(Class<E> clazz, Object id) {
        try {
            return em.find(clazz, id);
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see dk.dma.embryo.common.persistence.Dao#remove(dk.dma.embryo.common.persistence.IEntity)
     */
    @Override
    public void remove(IEntity<?> entity) {
        em.remove(em.merge(entity));
    }

    /* (non-Javadoc)
     * @see dk.dma.embryo.common.persistence.Dao#saveEntity(dk.dma.embryo.common.persistence.IEntity)
     */
    @Override
    public <E extends IEntity<?>> E saveEntity(E entity) {
        if (entity.isPersisted()) {
            // Update existing
            entity = em.merge(entity);
        } else {
            // Save new
            em.persist(entity);
        }
        
        return entity;
    }
    
    /* (non-Javadoc)
     * @see dk.dma.embryo.common.persistence.Dao#saveEntityWithFlush(dk.dma.embryo.common.persistence.IEntity)
     */
    @Override
    public <E extends IEntity<?>> E saveEntityWithFlush(E entity) {
        if (entity.isPersisted()) {
            // Update existing
            entity = em.merge(entity);
        } else {
            // Save new
            em.persist(entity);
        }
        
        em.flush();
        return entity;
    }


    /**
     * Gets the single or null.
     *
     * @param <T> the generic type
     * @param list the list
     * @return the single or null
     */
    public static <T> T getSingleOrNull(List<T> list) {
        return (list == null || list.size() == 0) ? null : list.get(0);
    }

    /* (non-Javadoc)
     * @see dk.dma.embryo.common.persistence.Dao#getAll(java.lang.Class)
     */
    public <E extends IEntity<?>> List<E> getAll(Class<E> entityType) {
        em.clear();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<E> cq = cb.createQuery(entityType);
        cq.from(entityType);
        return em.createQuery(cq).getResultList();
    }

    /* (non-Javadoc)
     * @see dk.dma.embryo.common.persistence.Dao#count(java.lang.Class)
     */
    public <E extends IEntity<?>> Long count(Class<E> entityType) {
        em.clear();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<E> root = cq.from(entityType);
        cq.select(cb.countDistinct(root));
        return em.createQuery(cq).getSingleResult();
    }
    
    /* (non-Javadoc)
     * @see dk.dma.embryo.common.persistence.Dao#flush()
     */
    public void flush() {
      em.flush();
    }
}
