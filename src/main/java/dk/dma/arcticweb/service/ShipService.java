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

import dk.dma.embryo.domain.Route;
import dk.dma.embryo.domain.Sailor;
import dk.dma.embryo.domain.Ship2;
import dk.dma.embryo.domain.ShipReport;
import dk.dma.embryo.domain.VoyageInformation2;

@Local
public interface ShipService {

    
    /**
     * Get the ship for the currently logged in {@link Sailor}.
     * 
     * @return
     */
    Ship2 getYourShip();
    
    /**
     * Add ship report for ship
     * 
     * @param ship
     * @param shipReport
     */
    void reportForCurrentShip(ShipReport shipReport);

    /**
     * Save voyage information
     * 
     * @param ship
     * @param voyageInformation
     */
    void saveVoyageInformation(VoyageInformation2 voyageInformation);
    
    VoyageInformation2 getVoyageInformation(Long mmsi);
    
    void saveRoute(Route route);
    
    Route getActiveRoute(Long mmsi);

}
