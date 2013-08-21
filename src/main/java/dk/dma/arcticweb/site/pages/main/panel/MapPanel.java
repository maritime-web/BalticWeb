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
import org.apache.wicket.markup.head.OnLoadHeaderItem;

import dk.dma.embryo.site.panel.EmbryonicPanel;

@SuppressWarnings("serial")
public class MapPanel extends EmbryonicPanel {

    private static final String JS_INIT = "embryo.mapPanel.init('projection');";

    public static final OnLoadHeaderItem MAP_INIT;

    static {
        String js_init = JS_INIT.replaceAll("projection", "EPSG:900913");
        MAP_INIT = OnLoadHeaderItem.forScript(js_init);
    }

    public MapPanel(String id) {
        super(id);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        // initialize component
        response.render(MAP_INIT);
    }
}
