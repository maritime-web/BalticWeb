package dk.dma.arcticweb.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;

import dk.dma.arcticweb.domain.IEntity;

@Stateless
public class DaoImpl implements Dao {

	@PersistenceContext(name = "arcticweb")
    protected EntityManager em;
	
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
