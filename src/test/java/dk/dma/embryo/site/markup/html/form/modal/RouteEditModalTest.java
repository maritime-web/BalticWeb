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
package dk.dma.embryo.site.markup.html.form.modal;

import javax.enterprise.inject.Produces;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import dk.dma.arcticweb.service.GeographicService;
import dk.dma.arcticweb.service.ShipService;
import dk.dma.arcticweb.site.CdiUnitTestBase;
import dk.dma.embryo.domain.Ship2;
import dk.dma.embryo.site.markup.html.dialog.Modal;

public class RouteEditModalTest extends CdiUnitTestBase{

    @Produces
    @Mock
    GeographicService geoService;
    
    @Produces
    @Mock
    ShipService shipService;

    @Test
    public void construction() {
        // ///////////////////////////////////////////////////
        // Setup test data
        // ///////////////////////////////////////////////////
        Ship2 ship = new Ship2(10L);

        // ///////////////////////////////////////////////////
        // Setup ShipService stub to return test data
        // ///////////////////////////////////////////////////
        Mockito.when(shipService.getYourShip()).thenReturn(ship);
        Mockito.when(shipService.getVoyageInformation(ship.getMmsi())).thenReturn(null);

        RouteEditModal modal = new RouteEditModal("someId").title("My title").size(Modal.SIZE.XLARGE);
        
        getTester().startComponentInPage(modal);

        getTester().assertComponent(modal.getPageRelativePath(), Modal.class);
        getTester().assertLabel(modal.getPageRelativePath() + ":modalContainer:title", "My title");
        getTester().assertComponent(modal.getPageRelativePath() + ":modalContainer:footer", WebMarkupContainer.class);
       
    }

}
