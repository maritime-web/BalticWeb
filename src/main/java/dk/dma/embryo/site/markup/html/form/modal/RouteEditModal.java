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
package dk.dma.embryo.site.markup.html.form.modal;

import java.util.List;

import javax.inject.Inject;

import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PropertyListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

import dk.dma.arcticweb.service.GeographicService;
import dk.dma.embryo.domain.Berth;
import dk.dma.embryo.domain.Voyage;
import dk.dma.embryo.security.authorization.YourShip;
import dk.dma.embryo.site.behavior.TypeaheadDataSource;
import dk.dma.embryo.site.behavior.TypeaheadDatum;
import dk.dma.embryo.site.converter.StyleDateConverter;
import dk.dma.embryo.site.markup.html.dialog.Modal;
import dk.dma.embryo.site.markup.html.form.DateTimeTextField;
import dk.dma.embryo.site.markup.html.form.TypeaheadTextField;
import dk.dma.embryo.site.markup.html.menu.ReachedFromMenu;

@YourShip
public class RouteEditModal extends Modal<RouteEditModal> implements ReachedFromMenu{

    private static final long serialVersionUID = 1L;

//    private final WebMarkupContainer voyageInformation;
   
   private FeedbackPanel feedback;
   
   private Form form;
   
   @Inject
   private Logger logger;

   @Inject
   private GeographicService geoService;

    
   /**
    * Modal panel for editing route information 
    * 
    * Default size is {@link SIZE#XLARGE}
    * @param id
    */
    public RouteEditModal(String id) {
        super(id);

        size(SIZE.XLARGE);
        title("Current Route");
        
        modalContainer.add(form = new Form<>("routeForm"));

        feedback = new FeedbackPanel("feedback", new ContainerFeedbackMessageFilter(form));
        feedback.setOutputMarkupId(true);
        feedback.setVisible(false);
        form.add(feedback);

        TypeaheadDataSource<TypeaheadDatum> dataSource = new BerthTypeaheadDataSource();
        
        form.add(new TextField<>("name"));
        form.add(new TypeaheadTextField<Berth, TypeaheadDatum>("origin", dataSource));
        form.add(new TypeaheadTextField<Berth, TypeaheadDatum>("destination", dataSource));

        form.add(DateTimeTextField.forDateStyle("etaOfDeparture", true, StyleDateConverter.DEFAULT_DATE_TIME));
        form.add(DateTimeTextField.forDateStyle("etaOfArrival", true, StyleDateConverter.DEFAULT_DATE_TIME));

        PropertyListView lv = new PropertyListView<Voyage>("routes") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<Voyage> item) {
            }
        };
        lv.setReuseItems(true);

        form.add(lv);
        
//        voyageInformation = new WebMarkupContainer("voyageInformation");
//        add(voyageInformation);
//        
//
//        voyageInformation.add(form);        
    }

    @Override
    public String getBookmark() {
        return modalContainer.getMarkupId();
    }
    
    private class BerthTypeaheadDataSource implements TypeaheadDataSource<TypeaheadDatum>{

        private static final long serialVersionUID = 1L;

        @Override
        public List<TypeaheadDatum> prefetch() {
            List<Berth> berths = geoService.findBerths("");
            List<TypeaheadDatum> transformed = Lists.transform(berths, new BerthTransformerFunction());
            return transformed;
        }

        @Override
        public List<TypeaheadDatum> remoteFetch(String query) {
            List<Berth> berths = geoService.findBerths(query);
            List<TypeaheadDatum> transformed = Lists.transform(berths, new BerthTransformerFunction());
            return transformed;
        }
    }
}
