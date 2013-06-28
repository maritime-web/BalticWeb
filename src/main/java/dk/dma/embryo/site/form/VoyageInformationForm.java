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
package dk.dma.embryo.site.form;

import java.util.List;

import javax.inject.Inject;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.IFormSubmitter;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PropertyListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.validation.validator.RangeValidator;
import org.slf4j.Logger;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import dk.dma.arcticweb.service.GeographicService;
import dk.dma.arcticweb.service.ShipService;
import dk.dma.arcticweb.site.pages.main.MainPage;
import dk.dma.embryo.domain.Berth;
import dk.dma.embryo.domain.Ship2;
import dk.dma.embryo.domain.Voyage;
import dk.dma.embryo.domain.VoyageInformation2;
import dk.dma.embryo.site.behavior.TypeaheadDataSource;
import dk.dma.embryo.site.component.TypeaheadTextField;
import dk.dma.embryo.site.panel.EmbryonicForm;

public class VoyageInformationForm extends EmbryonicForm<VoyageInformationForm> {

    private static final long serialVersionUID = 1L;

    private static final String JS_INIT = "embryo.voyageInformationForm.init('#id');";
    private final String js_init;

    private TextField<Integer> personsOnboard;
    private CheckBox doctorOnboard;

    private FeedbackPanel feedback;
    private AjaxSubmitLink saveLink;

    @Inject
    private ShipService shipService;

    @Inject
    private GeographicService geoService;

    @Inject
    private Logger logger;

    private DynamicPropertyListView<Voyage> lv;

    private WebMarkupContainer modalBody;

    private CompoundPropertyModel<VoyageInformation2> model = new CompoundPropertyModel<>(new VoyageInformationModel());

    public VoyageInformationForm(String id) {
        super(id, "Voyage Information");

        modalBody = new WebMarkupContainer("modalBody", model);
        add(modalBody);

        js_init = JS_INIT.replaceAll("id", modalBody.getMarkupId());

        personsOnboard = new TextField<>("personsOnboard");
        // With Wicket 6.7.0
        personsOnboard.add(new RangeValidator<Integer>(1, 10000));
        // With Wicket 1.5.8
        // personsOnboard.setRequired(true).add(new MinimumValidator<Integer>(1)).add(new
        // MaximumValidator<Integer>(10000));

        doctorOnboard = new CheckBox("doctorOnboard");

        feedback = new FeedbackPanel("voyage_information_feedback");
        feedback.setVisible(false);

        modalBody.add(personsOnboard);
        modalBody.add(doctorOnboard);
        modalBody.add(feedback);

        initializeListView(modalBody);

        saveLink = new AjaxSubmitLink("save") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                AjaxCallListener listener = new AjaxCallListener();
                listener.onBefore("embryo.voyageInformationForm.prepareRequest('#" + modalBody.getMarkupId() + "')");
                attributes.getAjaxCallListeners().add(listener);
            }

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                VoyageInformation2 info = model.getObject();
                // HACK an empty voyage was added, when loaded to generate empty row. Remove again before saving
                info.removeLastVoyage();

                shipService.saveVoyageInformation(info);
                feedback.setVisible(false);
                target.add(this.getParent());
                setResponsePage(new MainPage());
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                feedback.setVisible(true);
                target.add(this.getParent());
            }
        };

        add(saveLink);
    }

    private void initializeListView(WebMarkupContainer modalBody) {
        lv = new DynamicPropertyListView<Voyage>("voyagePlan") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<Voyage> item) {
                item.add(new HiddenField<String>("businessId"));
                item.add(new TypeaheadTextField<String, List<JsonBerth>>("berthName",
                        new TypeaheadDataSource<List<JsonBerth>>() {
                            private static final long serialVersionUID = 565818402802493124L;

                            @Override
                            public List<JsonBerth> remoteFetch(String query) {
                                logger.debug("query={}", query);

                                List<Berth> berths = geoService.findBerths(query);

                                logger.debug("berths={}", berths);
                                
                                List<JsonBerth> transformed = Lists.transform(berths, new BerthTransformerFunction());
                                return transformed;
                            }

                            @Override
                            public List<JsonBerth> prefetch() {
                                List<Berth> berths = geoService.findBerths("");
                                
                                logger.debug("berths={}", berths);
                                
                                List<JsonBerth> transformed = Lists.transform(berths, new BerthTransformerFunction());
                                return transformed;
                            }
                        }));
                item.add(new TextField<String>("position.latitude"));
                item.add(new TextField<String>("position.longitude"));

                AttributeModifier dateFormat = new AttributeModifier("placeholder", "MM/dd/yyyy");

                item.add(new TextField<String>("arrival").add(dateFormat));
                item.add(new TextField<String>("departure").add(dateFormat));
            }
        };
        lv.setReuseItems(false);

        modalBody.add(lv);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        // initialize component
        response.render(OnLoadHeaderItem.forScript(js_init));
    }

    @Override
    public void process(IFormSubmitter submittingComponent) {
        // update request being treated.
        // create empty voyage information objects as of no value to load them from database. They will be
        // overwritten later on in any case.
        IModel<?> chainedModel = model.getChainedModel();
        model.setChainedModel(new EmptyVoyageInformationModel());
        lv.rebuild();
        super.process(submittingComponent);
        model.setChainedModel(chainedModel);
    }

    public abstract static class DynamicPropertyListView<T> extends PropertyListView<T> {

        private static final long serialVersionUID = 6599673322320525132L;

        public DynamicPropertyListView(String id) {
            super(id);
        }

        public void rebuild() {
            removeAll();
            onPopulate();
        };
    }

    public class EmptyVoyageInformationModel extends Model<VoyageInformation2> {
        private static final long serialVersionUID = -8287432934320097962L;

        public EmptyVoyageInformationModel() {
            IRequestParameters parameters = RequestCycle.get().getRequest().getRequestParameters();
            if (parameters.getParameterNames().contains("voyageCount")) {
                // update request being treated.
                // create empty voyage information objects as of no value to load them from database. They will be
                // overwritten later on in any case.
                Integer count = parameters.getParameterValue("voyageCount").toInteger();
                Ship2 ship = shipService.getYourShip();
                VoyageInformation2 info = new VoyageInformation2();
                ship.setVoyageInformation(info);
                for (int i = 0; i < count; i++) {
                    info.addVoyageEntry(new Voyage());
                }
                setObject(info);
            } else {
                throw new IllegalStateException("request parameter voyageCount missing");
            }
        }
    }

    public class VoyageInformationModel extends LoadableDetachableModel<VoyageInformation2> {

        private static final long serialVersionUID = 5624306986147386672L;

        @Override
        protected VoyageInformation2 load() {
            // Initial request. Load voyage information from database.
            Ship2 ship = shipService.getYourShip();
            VoyageInformation2 info = shipService.getVoyageInformation(ship.getMmsi());
            // HACK: Add empty voyage to generate empty row in case the user creates new voyages
            info.addVoyageEntry(new Voyage(null));
            return info;
        }
    }

    public static class BerthTransformerFunction implements Function<Berth, JsonBerth> {
        @Override
        public JsonBerth apply(Berth input) {
            return new JsonBerth(input.getName(), input.getAlias(), input.getPosition().asGeometryPosition()
                    .getLatitudeAsString(), input.getPosition().asGeometryPosition().getLongitudeAsString());
        }
    }

    public static class JsonBerth {
        private String name;
        private String alias;
        private String latitude;
        private String longitude;

        public JsonBerth(String name, String alias, String latitude, String longitude) {
            super();
            this.name = name;
            this.alias = alias;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getName() {
            return name;
        }

        public String getAlias() {
            return alias;
        }

        public String getLatitude() {
            return latitude;
        }

        public String getLongitude() {
            return longitude;
        }
    }
}
