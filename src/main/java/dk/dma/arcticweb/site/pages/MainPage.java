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
package dk.dma.arcticweb.site.pages;

import dk.dma.arcticweb.site.SecurePage;
import dk.dma.arcticweb.site.pages.main.panel.JsPanel;
import dk.dma.arcticweb.site.pages.main.panel.MapPanel;
import dk.dma.arcticweb.site.pages.main.panel.SelectedShipInformationPanel;
import dk.dma.arcticweb.site.pages.main.panel.ShipReportPanel;
import dk.dma.arcticweb.site.pages.main.panel.StatusPanel;
import dk.dma.arcticweb.site.pages.main.panel.UserPanel;
import dk.dma.embryo.site.markup.html.form.modal.RouteEdit;
import dk.dma.embryo.site.markup.html.form.modal.RouteUploadModal;
import dk.dma.embryo.site.markup.html.menu.MenuHeader;
import dk.dma.embryo.site.markup.html.menu.MenuPanel;
import dk.dma.embryo.site.panel.DrawTestRoutesExecutor;
import dk.dma.embryo.site.panel.LeftPanel;
import dk.dma.embryo.site.panel.ShipInformationPanel;
import dk.dma.embryo.site.panel.VoyageInformationPanel;
import dk.dma.embryo.site.panel.ZoomToShipJSExecutor;

public class MainPage extends BasePage implements SecurePage {

    private static final long serialVersionUID = 1L;

    public MainPage() {
        super();

        MapPanel mapPanel = new MapPanel("map");
        add(mapPanel);
        mapPanel.addComponent(LeftPanel.class);
        mapPanel.addComponent(StatusPanel.class);
        
        add(new UserPanel("user_panel"));

        ShipInformationPanel shipInformation = new ShipInformationPanel("ship_information");
        ShipReportPanel shipReport = new ShipReportPanel("ship_report");
        VoyageInformationPanel voyageInformation = new VoyageInformationPanel("voyage_information");

        RouteEdit routeModal = new RouteEdit("routeModule");
        RouteUploadModal routeUpload = new RouteUploadModal("routeUpload");

        
        // /////////////////////////////////////////////////
        // Build up menu
        // /////////////////////////////////////////////////
        MenuPanel menuPanel = new MenuPanel("menu_panel");
        add(menuPanel);

        // Your Ship
        MenuHeader yourShip = menuPanel.addMenuHeader("Your Ship");
        yourShip.addMenuItem("Zoom to ship", new ZoomToShipJSExecutor());
        yourShip.addMenuItem(shipInformation);
        yourShip.addMenuItem(shipReport);
        yourShip.addMenuItem(voyageInformation);
        yourShip.addMenuItem(routeModal);
        yourShip.addMenuItem(routeUpload);
        yourShip.addMenuItem("Draw Route 231", new DrawTestRoutesExecutor());

        add(new JsPanel("js_panel"));

        // add(new LeftPanel2("left"));
        // add(new StatusPanel("status"));

        add(shipInformation, shipReport, voyageInformation, routeModal, routeUpload);

        add(new SelectedShipInformationPanel("selected_ship_information"));
    }

}
