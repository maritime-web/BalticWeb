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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

import dk.dma.arcticweb.service.GeographicService;
import dk.dma.arcticweb.service.ShipService;
import dk.dma.arcticweb.site.pages.MainPage;
import dk.dma.embryo.domain.Berth;
import dk.dma.embryo.domain.Route;
import dk.dma.embryo.domain.Ship2;
import dk.dma.embryo.domain.WayPoint;
import dk.dma.embryo.security.authorization.YourShip;
import dk.dma.embryo.site.behavior.TypeaheadDataSource;
import dk.dma.embryo.site.behavior.TypeaheadDatum;
import dk.dma.embryo.site.converter.StyleDateConverter;
import dk.dma.embryo.site.markup.html.dialog.Modal;
import dk.dma.embryo.site.markup.html.form.DateTimeTextField;
import dk.dma.embryo.site.markup.html.form.DynamicListView;
import dk.dma.embryo.site.markup.html.form.PropertyDynamicListView;
import dk.dma.embryo.site.markup.html.form.TypeaheadTextField;
import dk.dma.embryo.site.markup.html.menu.ReachedFromMenu;

@YourShip
public class RouteEditModal extends Modal<RouteEditModal> implements ReachedFromMenu {

    private static final long serialVersionUID = 1L;

    private FeedbackPanel feedback;

    private Form form;

    @Inject
    private Logger logger;

    @Inject
    private GeographicService geoService;

    @Inject
    private ShipService shipService;

    private CompoundPropertyModel<Route> model = new CompoundPropertyModel<>(new RouteModel());

    /**
     * Modal panel for editing route information
     * 
     * Default size is {@link SIZE#XLARGE}
     * 
     * @param id
     */
    public RouteEditModal(String id) {
        super(id);

        size(SIZE.XLARGE);
        title("Current Route");

        modalContainer.add(form = new Form<>("routeForm", model));

        feedback = new FeedbackPanel("feedback", new ContainerFeedbackMessageFilter(form));
        feedback.setOutputMarkupId(true);
        feedback.setVisible(false);
        form.add(feedback);

        TypeaheadDataSource<TypeaheadDatum> dataSource = new BerthTypeaheadDataSource();

        form.add(new TextField<>("name"));
        form.add(new HiddenField<>("voyage.id"));
        form.add(new TextField<>("voyageName"));

        form.add(new TypeaheadTextField<Berth, TypeaheadDatum>("origin", dataSource));
        form.add(new TypeaheadTextField<Berth, TypeaheadDatum>("destination", dataSource));

        form.add(DateTimeTextField.forDateStyle("etaOfDeparture", true, StyleDateConverter.DEFAULT_DATE_TIME));
        form.add(DateTimeTextField.forDateStyle("etaOfArrival", true, StyleDateConverter.DEFAULT_DATE_TIME));

        final DynamicListView<WayPoint> lv = new PropertyDynamicListView<WayPoint>("wayPoints") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<WayPoint> item) {
                item.add(new TextField<>("name"));
                item.add(new TextField<>("position.latitude"));
                item.add(new TextField<>("position.longitude"));
                item.add(new TextField<>("turnRadius"));
            }
            
        }.setInstanceType(WayPoint.class).setAutoExpand(false);

        form.add(lv);

        AjaxSubmitLink saveLink = new AjaxSubmitLink(Modal.FOOTER_BUTTON, form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                // TODO try to remove this method and move logic into dynamic list component
                AjaxCallListener listener = new AjaxCallListener();
                listener.onBefore("embryo.dynamicListView.prepareRequest('#" + lv.getMarkupId() + "')");
                attributes.getAjaxCallListeners().add(listener);
            }

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                Route route = model.getObject();
                // HACK an empty voyage was added, when loaded to generate empty row. Remove again before saving
                // Try to move functionality into dynamic list component
                // route.removeLastVoyage();

                // Hack to make it work (mmsi search needs mmsi number)
                route.setShip(shipService.getYourShip());
                
                shipService.saveRoute(route);
                feedback.setVisible(false);
                target.add(form);
                setResponsePage(new MainPage());
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                feedback.setVisible(true);
                target.add(this.getParent());
            }
        };
        
        saveLink.add(AttributeModifier.append("class", " btn btn-primary"));

        addFooterButton(saveLink);

    }

    @Override
    public String getBookmark() {
        return modalContainer.getMarkupId();
    }

    private class BerthTypeaheadDataSource implements TypeaheadDataSource<TypeaheadDatum> {

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

    public class RouteModel extends LoadableDetachableModel<Route> {

        private static final long serialVersionUID = 5624306986147386672L;

        @Override
        protected Route load() {
            Ship2 ship = shipService.getYourShip();
            Route route = shipService.getActiveRoute(ship.getMmsi());
            
            if(route == null){
                route = new Route();
            }
            // HACK: Add empty voyage to generate empty row in case the user creates new voyages
            // route.addVoyageEntry(new Voyage(null));
            return route;
        }
    }
}
