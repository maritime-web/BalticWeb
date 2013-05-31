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
     * 
     * @param user
     * @return
     */
    <SH extends Stakeholder> SH getStakeholder(User user);

    /**
     * Persist stakeholder
     * 
     * @param stakeholder
     * @return
     */
    Stakeholder save(Stakeholder stakeholder);

    /**
     * Add ship report for ship
     * 
     * @param ship
     * @param shipReport
     */
    void addShipReport(Ship ship, ShipReport shipReport);

    /**
     * Get or create voyage information for ship
     * 
     * @param ship
     * @return
     */
    VoyageInformation getVoyageInformation(Ship ship);

    /**
     * Save voyage information
     * 
     * @param ship
     * @param voyageInformation
     */
    void saveVoyageInformation(Ship ship, VoyageInformation voyageInformation);

}
