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
import org.apache.wicket.markup.html.basic.Label;

import dk.dma.embryo.site.panel.EmbryonicPanel;

@SuppressWarnings("serial")
public class MenuTestPanel extends EmbryonicPanel implements ReachedFromMenu{

    
    WebMarkupContainer container;
    
    
    public MenuTestPanel(String id) {
        this(id, "Test Panel");
    }
    public MenuTestPanel(String id, String title) {
        super(id, title);

        container = new WebMarkupContainer("container");
        container.add(new Label("text", "label"));
    }

    @Override
    public String getBookmark() {
        return container.getMarkupId();
    }
    
    
}
