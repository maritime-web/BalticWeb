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
package dk.dma.embryo.site.panel;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

import dk.dma.arcticweb.site.pages.main.panel.LegendsPanel;
import dk.dma.arcticweb.site.pages.main.panel.SearchPanel;
import dk.dma.arcticweb.site.pages.main.panel.VesselDetailsPanel;
import dk.dma.embryo.security.authorization.Ais;

@Ais
@SuppressWarnings("serial")
public class LeftPanel2 extends Panel {

    private final RepeatingView rw;

    private static final String JS_INIT = "embryo.leftPanel.init();";

    public LeftPanel2(String id) {
        super(id);

        System.out.println("LeftPanel2(" + id + ")");

        rw = new RepeatingView("collapseGroup");
        add(rw);

        String collapsableGroupId = rw.newChildId();
        add("Legends", collapsableGroupId, new LegendsPanel("collapsableContent"));

        collapsableGroupId = rw.newChildId();
        add("Search", collapsableGroupId, new SearchPanel("collapsableContent"));
        //
        collapsableGroupId = rw.newChildId();
        add("Vessel details", collapsableGroupId, new VesselDetailsPanel("collapsableContent"));
    }

    private void add(String header, String collapsableGroupId, Component content) {
        WebMarkupContainer collapsableGroup = new WebMarkupContainer(collapsableGroupId);
        rw.add(collapsableGroup);

        WebMarkupContainer collapsable = new WebMarkupContainer("collapsable");
        // Get auto generated html id
        String idSelector = "#" + collapsable.getMarkupId();
        collapsable.add(content);

        Label headerText = new Label("collapseHeader", header);
        headerText.add(new AttributeModifier("data-target", idSelector));
        // ExternalLink headerLink = new ExternalLink(, idSelector);
        // headerLink.add(new AttributeModifier("data-target", idSelector));

        collapsableGroup.add(collapsable);
        // collapsableGroup.add(headerLink);
        collapsableGroup.add(headerText);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        // if component is disabled we don't have to load the JQueryUI datepicker
        if (!isEnabledInHierarchy()) {
            return;
        }
        // initialize component
        response.render(OnLoadHeaderItem.forScript(JS_INIT));
    }

}
