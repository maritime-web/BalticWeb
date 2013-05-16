package dk.dma.arcticweb.dao;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;

import dk.dma.arcticweb.domain.Authority;
import dk.dma.arcticweb.domain.IEntity;
import dk.dma.arcticweb.domain.Ship;
import dk.dma.arcticweb.domain.ShipOwner;
import dk.dma.arcticweb.domain.ShoreStakeholder;
import dk.dma.arcticweb.domain.Stakeholder;

@Stateless
public class StakeholderDaoImpl extends DaoImpl implements StakeholderDao {

	@SuppressWarnings("unchecked")
	@Override
	public List<Stakeholder> getAll() {
		Query query = em.createQuery("SELECT s FROM Stakeholder s");
		return query.getResultList();
	}
	
	@Override
	public Stakeholder get(int id) {
		// Special handling to get instance of the correct type
		List<Class<? extends IEntity>> stakeholderClasses = new ArrayList<>();
		stakeholderClasses.add(Ship.class);
		stakeholderClasses.add(Authority.class);
		stakeholderClasses.add(ShipOwner.class);
		stakeholderClasses.add(ShoreStakeholder.class);		
		for (Class<? extends IEntity> stakeholderClass : stakeholderClasses) {
			IEntity stakeholder = getByPrimaryKey(stakeholderClass, id);
			if (stakeholder != null) {
				return (Stakeholder)stakeholder;
			}
		}		
		return null;
	}
	
	

}
