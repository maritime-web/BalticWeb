/* Copyright (c) 2012 Danish Maritime Authority
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
package dk.dma.arcticweb.site.pages.main;

import org.apache.wicket.markup.html.WebMarkupContainer;

import dk.dma.arcticweb.site.SecurePage;
import dk.dma.arcticweb.site.pages.BasePage;
import dk.dma.arcticweb.site.pages.main.panel.JsPanel;
import dk.dma.arcticweb.site.pages.main.panel.MapPanel;
import dk.dma.arcticweb.site.pages.main.panel.MenuPanel;
import dk.dma.arcticweb.site.pages.main.panel.SelectedShipInformationPanel;
import dk.dma.arcticweb.site.pages.main.panel.ShipInformationPanel;
import dk.dma.arcticweb.site.pages.main.panel.ShipReportPanel;
import dk.dma.arcticweb.site.pages.main.panel.UserPanel;
import dk.dma.arcticweb.site.pages.main.panel.VoyageInformationPanel;
import dk.dma.arcticweb.site.session.ArcticWebSession;

public class MainPage extends BasePage implements SecurePage {
	private static final long serialVersionUID = 1L;
		
	public MainPage() {
		super();		
		add(new UserPanel("user_panel"));
		add(new MapPanel("map"));
		add(new MenuPanel("menu_panel"));
		add(new JsPanel("js_panel"));
		
		if (ArcticWebSession.get().getStakeholder().isShip()) {
			add(new ShipInformationPanel("ship_information"));		
			add(new ShipReportPanel("ship_report"));
			add(new VoyageInformationPanel("voyage_information"));
		} else {
			add(new WebMarkupContainer("ship_information"));		
			add(new WebMarkupContainer("ship_report"));
			add(new WebMarkupContainer("voyage_information"));
		}
		
		add(new SelectedShipInformationPanel("selected_ship_information"));
	}

}
