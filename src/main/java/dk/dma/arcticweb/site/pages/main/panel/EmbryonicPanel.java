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

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

import dk.dma.embryo.site.component.ComponentFactory;
import dk.dma.embryo.site.component.ReflectiveComponentFactory;

public abstract class EmbryonicPanel extends Panel implements ComponentFactory {

    private static final long serialVersionUID = 4132041261965905788L;

    public static final String OVERLAY_COMPONENT_GROUP_ID = "childComponentGroup";
    public static final String OVERLAY_COMPONENT_ID = "childComponent";

    private final RepeatingView rw;

    @Inject
    transient ReflectiveComponentFactory factory;

    public EmbryonicPanel(String id) {
        super(id);
        rw = new RepeatingView(OVERLAY_COMPONENT_GROUP_ID);
        add(rw);
    }

    @Override
    public <CT extends Component> CT addComponent(Class<CT> componentType) {
        return addComponent(componentType, OVERLAY_COMPONENT_ID);
    }

    protected <CT extends Component> CT addComponent(Class<CT> componentType, String overlayComponentId) {
        WebMarkupContainer collapsableGroup = new WebMarkupContainer(rw.newChildId());
        rw.add(collapsableGroup);

        CT component = factory.createComponent(componentType, overlayComponentId);
        collapsableGroup.add(component);

        return component;
    }
}
