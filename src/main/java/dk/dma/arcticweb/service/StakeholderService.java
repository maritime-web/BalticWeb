package dk.dma.arcticweb.service;

import javax.ejb.Local;

import dk.dma.arcticweb.domain.Ship;
import dk.dma.arcticweb.domain.ShipReport;
import dk.dma.arcticweb.domain.Stakeholder;
import dk.dma.arcticweb.domain.User;
import dk.dma.arcticweb.domain.VoyageInformation;

@Local
public interface StakeholderService {
	
	/**
	 * Get stakeholder given user
	 * @param user
	 * @return
	 */
	Stakeholder getStakeholder(User user);
	
	/**
	 * Persist stakeholder
	 * @param stakeholder
	 * @return
	 */
	Stakeholder save(Stakeholder stakeholder);

	/**
	 * Add ship report for ship
	 * @param ship
	 * @param shipReport
	 */
	void addShipReport(Ship ship, ShipReport shipReport);

	/**
	 * Get or create voyage information for ship
	 * @param ship
	 * @return
	 */
	VoyageInformation getVoyageInformation(Ship ship);

	/**
	 * Save voyage information
	 * @param ship 
	 * @param voyageInformation
	 */
	void saveVoyageInformation(Ship ship, VoyageInformation voyageInformation);
	
}
