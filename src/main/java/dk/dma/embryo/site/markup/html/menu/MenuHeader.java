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

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;

import dk.dma.embryo.security.authorization.DependsOnChildPermissionContainers;
import dk.dma.embryo.site.panel.JSExecutor;

@DependsOnChildPermissionContainers
public class MenuHeader extends MarkupContainer{

    private static final long serialVersionUID = 1L;

    private final RepeatingView rw;

    public MenuHeader(String id, String menuHeaderText) {
        super(id);
        
        add(new Label("menuHeaderText", menuHeaderText));
        
        rw = new RepeatingView("menuItem");
        add(rw);

    }
    
    /**
     * Adds a menu item, which can popup a panel of some kind. 
     * 
     * @param menuItemText
     * @param panel
     * @return
     */
    public MenuItem addMenuItem(ReachedFromMenu panel){
        String menuItemId = rw.newChildId();
        MenuItem menuItem = new MenuItem(menuItemId,panel);
        rw.add(menuItem);
        return menuItem;
    }

    public MenuItem addMenuItem(String title, JSExecutor executor){
        String menuItemId = rw.newChildId();
        MenuItem menuItem = new MenuItem(menuItemId,title, executor);
        rw.add(menuItem);
        return menuItem;
    }

}
