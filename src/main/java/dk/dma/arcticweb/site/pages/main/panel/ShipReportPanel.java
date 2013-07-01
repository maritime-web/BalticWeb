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

import org.apache.wicket.markup.html.WebMarkupContainer;

import dk.dma.arcticweb.site.pages.main.form.ShipReportForm;
import dk.dma.embryo.security.authorization.YourShip;
import dk.dma.embryo.site.markup.html.menu.ReachedFromMenu;
import dk.dma.embryo.site.panel.EmbryonicPanel;

@YourShip
public class ShipReportPanel extends EmbryonicPanel implements ReachedFromMenu{

    private static final long serialVersionUID = 1L;

    private final WebMarkupContainer shipReport;

    private final ShipReportForm form;
    
    
    public ShipReportPanel(String id) {
        super(id);
        
        shipReport = new WebMarkupContainer("shipReport");
        add(shipReport);

        form = new ShipReportForm("ship_report_form");
        
        shipReport.add(form);
    }

    
    public String getTitle(){
        return form.getTitle();
    }
    
    @Override
    public String getBookmark() {
        return shipReport.getMarkupId();
    }

}
