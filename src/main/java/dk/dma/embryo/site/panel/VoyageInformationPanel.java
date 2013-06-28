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

import org.apache.wicket.markup.html.WebMarkupContainer;

import dk.dma.embryo.security.authorization.YourShip;
import dk.dma.embryo.site.form.VoyageInformationForm;

@YourShip
public class VoyageInformationPanel extends EmbryonicPanel implements ReachedFromMenu{

    private static final long serialVersionUID = 1L;

    private final WebMarkupContainer voyageInformation;
   
    private final VoyageInformationForm form;
    

    public VoyageInformationPanel(String id) {
        super(id);
        
        voyageInformation = new WebMarkupContainer("voyageInformation");
        add(voyageInformation);
        
        form = new VoyageInformationForm("voyage_information_form");

        voyageInformation.add(form);        
    }

    public String getTitle(){
        return form.getTitle();
    }
    
    @Override
    public String getBookmark() {
        return voyageInformation.getMarkupId();
    }
}
