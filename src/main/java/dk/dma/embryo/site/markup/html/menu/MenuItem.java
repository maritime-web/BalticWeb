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

import javax.inject.Inject;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;

import dk.dma.embryo.security.PermissionContainer;
import dk.dma.embryo.security.PermissionExtractor;
import dk.dma.embryo.security.authorization.Permission;
import dk.dma.embryo.site.panel.JSExecutor;

public class MenuItem extends MarkupContainer implements PermissionContainer{

    private static final long serialVersionUID = 1L;

    @Inject
    private PermissionExtractor permissionExtractor;
    
    private Permission[] permissions;
    
    private WebMarkupContainer link = new WebMarkupContainer("menuItemLink");

    private MenuItem(String id, String title) {
        super(id);
        link = new WebMarkupContainer("menuItemLink");
        link.add(new Label("menuItemText", title));
        add(link);
    }

    public MenuItem(String id, ReachedFromMenu modal) {
        this(id, modal.getTitle());
        link.add(new AttributeModifier("href", "#" + modal.getBookmark()));
        link.add(new AttributeModifier("role", "button"));
        link.add(new AttributeModifier("data-toggle", "modal"));
        permissions = permissionExtractor.getPermissions(modal);
    }

    public MenuItem(String id, String title, JSExecutor executor) {
        this(id, title);
        executor.decorate(link);
        permissions = permissionExtractor.getPermissions(executor);
    }

    @Override
    public Permission[] getPermissions() {
        return permissions;
    }

    @Override
    public boolean hasPermissions() {
        return permissions != null && permissions.length > 0;
    }
}
