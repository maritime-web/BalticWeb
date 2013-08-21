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
package dk.dma.embryo.site.markup.html.menu;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

public class MenuPanel extends Panel {

    private static final long serialVersionUID = 1L;

    private final RepeatingView rw;

    private WebMarkupContainer selectedShip;

    public MenuPanel(String id) {
        super(id);

        rw = new RepeatingView("menuHeader");
        add(rw);
        
        selectedShip = new WebMarkupContainer("selected_ship");
        selectedShip.setVisible(true);

        add(selectedShip);
    }
    
    public MenuHeader addMenuHeader(String title){
        String menuHeaderId = rw.newChildId();
        MenuHeader menuHeader = new MenuHeader(menuHeaderId, title);
        rw.add(menuHeader);
        return menuHeader;
    }

}
