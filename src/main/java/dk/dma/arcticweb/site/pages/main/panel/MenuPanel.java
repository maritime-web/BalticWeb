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
package dk.dma.arcticweb.site.pages.main.panel;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;

import dk.dma.arcticweb.domain.Stakeholder;
import dk.dma.arcticweb.site.session.ArcticWebSession;

public class MenuPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	private WebMarkupContainer yourShip;
	private WebMarkupContainer selectedShip;
	
	public MenuPanel(String id) {
		super(id);
		
		Stakeholder stakeholder = ArcticWebSession.get().getStakeholder();
		
		yourShip = new WebMarkupContainer("your_ship");
		yourShip.setVisible(stakeholder.isShip());
		
		add(yourShip);
		
		selectedShip = new WebMarkupContainer("selected_ship");
		selectedShip.setVisible(true);
		
		add(selectedShip);
		
	}

}
