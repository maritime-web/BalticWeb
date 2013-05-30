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

import javax.ejb.EJB;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import dk.dma.arcticweb.domain.Ship;
import dk.dma.arcticweb.service.StakeholderService;
import dk.dma.arcticweb.site.session.ArcticWebSession;

public class JsPanel extends Panel {
	
	private static final long serialVersionUID = 1L;
	
	@EJB
	StakeholderService stakeholderService;

	public JsPanel(String id) {
		super(id);
		setRenderBodyOnly(true);

		// Get stakeholder type and possibly ship MMSI
		ArcticWebSession session = ArcticWebSession.get();		
		String stakeholderType = session.getStakeholder().getStakeholderType();		
		String shipMmsi = "null";
		if (session.getStakeholder() instanceof Ship) {
			shipMmsi = Long.toString(((Ship)session.getStakeholder()).getMmsi());
		}

		// Make label
		StringBuilder js = new StringBuilder();
		js.append("var stakeholder_type = '" + stakeholderType + "';\n");
		js.append("var ship_mmsi = " + shipMmsi + ";\n");
		add(new Label("js", "\n" + js.toString()).setEscapeModelStrings(false));
	}

}
