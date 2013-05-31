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

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;

import dk.dma.arcticweb.site.resources.OnLoadMapDependentHeaderItem;

@SuppressWarnings("serial")
public class VesselDetailsPanel extends Panel {

    private static final String JS_INIT = "embryo.vesselDetailsPanel.init();";

    public VesselDetailsPanel(String id) {
        super(id);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        // if component is disabled we don't have to load the JQueryUI datepicker
        if (!isEnabledInHierarchy()) {
            return;
        }
        // initialize component
        response.render(OnLoadMapDependentHeaderItem.forScript(JS_INIT));
    }

}
