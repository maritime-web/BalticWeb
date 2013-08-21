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
package dk.dma.embryo.site.panel;

import javax.enterprise.inject.Produces;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import dk.dma.arcticweb.service.GeographicService;
import dk.dma.arcticweb.service.ShipService;
import dk.dma.arcticweb.site.CdiUnitTestBase;
import dk.dma.embryo.domain.Ship2;
import dk.dma.embryo.domain.Voyage;
import dk.dma.embryo.domain.VoyagePlan;

public class VoyagePlanPanelTest extends CdiUnitTestBase {

    @Produces
    @Mock
    ShipService shipService;

    @Produces
    @Mock
    GeographicService geoService;

    @Test
    public void testConstruction() {
        // ///////////////////////////////////////////////////
        // Setup test data
        // ///////////////////////////////////////////////////
        Ship2 ship = new Ship2();

        VoyagePlan voaygePlan = new VoyagePlan(12, false);
        voaygePlan.addVoyageEntry(new Voyage("City1", "1 1.000N", "1 2.000W", LocalDateTime.parse("2013-06-19T12:23"),
                LocalDateTime.parse("2013-06-20T11:56")));
//        voaygeInformation.addVoyageEntry(new Voyage("City2", "3.300", "6.000", LocalDateTime.parse("2013-06-23T22:08"),
//                LocalDateTime.parse("2013-06-25T20:19")));

        // ///////////////////////////////////////////////////
        // Setup ShipService stub to return test data
        // ///////////////////////////////////////////////////
        Mockito.when(shipService.getYourShip()).thenReturn(ship);
        Mockito.when(shipService.getVoyagePlan(ship.getMmsi())).thenReturn(voaygePlan);

        // ///////////////////////////////////////////////////
        // Instantiate panel
        // ///////////////////////////////////////////////////
        VoyagePlanPanel panel = new VoyagePlanPanel("someId");
        getTester().startComponentInPage(panel);

        // ///////////////////////////////////////////////////
        // Verify
        // ///////////////////////////////////////////////////
        getTester().assertComponent(panel.getPageRelativePath(), VoyagePlanPanel.class);
        getTester().assertLabel(panel.getPageRelativePath() + ":voyagePlan:voyage_plan_form:title", "Voyage Plan");

        //getTester().assertModelValue(panel.getPageRelativePath() + ":voyagePlan:voyage_plan_form:personsOnboard", Integer.valueOf(12));
        

        //
        // getTester().assertLabel(header1.getPageRelativePath() + ":menuItem:1:menuItemLink:menuItemText", "Item 1");
        // getTester().assertLabel(header1.getPageRelativePath() + ":menuItem:2:menuItemLink:menuItemText", "Item 2");
        // getTester().assertLabel(header1.getPageRelativePath() + ":menuItem:3:menuItemLink:menuItemText", "Item 3");
    }

}
