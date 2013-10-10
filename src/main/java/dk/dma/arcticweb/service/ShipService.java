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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.ejb.Local;

import dk.dma.embryo.domain.Route;
import dk.dma.embryo.domain.Sailor;
import dk.dma.embryo.domain.Ship;
import dk.dma.embryo.domain.ShipReport;
import dk.dma.embryo.domain.Voyage;
import dk.dma.embryo.domain.VoyagePlan;

@Local
public interface ShipService {

    
    /**
     * Get the ship for the currently logged in {@link Sailor}.
     * 
     * @return
     */
    Ship getYourShip();

    /**
     * 
     * @param ship
     * @return
     */
    String save(Ship ship);

    /**
     * Add ship report for ship
     * 
     * @param ship
     * @param shipReport
     */
    void reportForCurrentShip(ShipReport shipReport);

    /**
     * Save voyage plan
     * 
     * @param ship
     * @param voyagePlan
     */
    void saveVoyagePlan(VoyagePlan voyagePlan);
    
    VoyagePlan getVoyagePlan(Long mmsi);
    
    List<Voyage> getVoyages(Long mmsi);

    Voyage getActiveVoyage(String maritimeId);

    String saveRoute(Route route, String voyageId, Boolean active);

    String saveRoute(Route route);

    Route getYourActiveRoute();

    Route getActiveRoute(Long mmsi);

    Route activateRoute(String routeEnavId, Boolean activate);

    Route getRouteByEnavId(String enavId);

    Voyage getVoyage(String businessId);
    
    Route parseRoute(InputStream is) throws IOException;

}
