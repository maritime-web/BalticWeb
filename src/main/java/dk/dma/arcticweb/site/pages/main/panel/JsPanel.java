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

import javax.inject.Inject;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import dk.dma.arcticweb.dao.RealmDao;
import dk.dma.embryo.domain.Sailor;
import dk.dma.embryo.security.Subject;

public class JsPanel extends Panel {

    private static final long serialVersionUID = 1L;

    @Inject
    private Subject subject;
    
    @Inject
    private RealmDao realmRepository;

    public JsPanel(String id) {
        super(id);
        setRenderBodyOnly(true);

        // Get stakeholder type and possibly ship MMSI
        String shipMmsi = "null";
        if (subject.hasRole(Sailor.class)) {
            Sailor sailor = realmRepository.getSailor(subject.getUserId());
            shipMmsi = Long.toString(sailor.getShip().getMmsi());
        }
//
        // Make label
        StringBuilder js = new StringBuilder();
//        js.append("var stakeholder_type = '" + stakeholderType + "';\n");
        js.append("var ship_mmsi = " + shipMmsi + ";\n");
        add(new Label("js", "\n" + js.toString()).setEscapeModelStrings(false));
    }

}
