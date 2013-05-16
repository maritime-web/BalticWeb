package dk.dma.arcticweb.dao;

import dk.dma.arcticweb.domain.IEntity;

public interface Dao {
	
	/**
	 * Get entity by primary key
	 * @param clazz
	 * @param id
	 * @return
	 */
	public IEntity getByPrimaryKey(Class<? extends IEntity> clazz, Object id);
	
	/**
	 * Remove entity
	 * @param bean
	 */
	public abstract void remove(IEntity bean);
	
	/**
	 * Save (insert or update) the entity bean
	 * 
	 * @param entity
	 * @return entity
	 */
	public abstract IEntity saveEntity(IEntity bean);
	
    
}
