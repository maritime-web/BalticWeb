package dk.dma.arcticweb.dao;

import java.util.List;

import javax.ejb.Local;

import dk.dma.arcticweb.domain.Stakeholder;

@Local
public interface StakeholderDao extends Dao {
	
	/**
	 * Get all stakeholders
	 * @return
	 */
	List<Stakeholder> getAll();
	
	/**
	 * Get stakeholder by id
	 * @param id
	 * @return
	 */
	Stakeholder get(int id);
	
}
