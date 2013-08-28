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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
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
import dk.dma.arcticweb.site.pages.MainPage;
import dk.dma.embryo.domain.Berth;
import dk.dma.embryo.domain.Ship2;
import dk.dma.embryo.domain.Voyage;
import dk.dma.embryo.domain.VoyagePlan;
import dk.dma.embryo.rest.util.TypeaheadDatum;
import dk.dma.embryo.site.behavior.TypeaheadDataSource;
import dk.dma.embryo.site.converter.StyleDateConverter;
import dk.dma.embryo.site.markup.html.form.DateTimeTextField;
import dk.dma.embryo.site.markup.html.form.LatitudeTextField;
import dk.dma.embryo.site.markup.html.form.LongitudeTextField;
import dk.dma.embryo.site.markup.html.form.TypeaheadTextField;
import dk.dma.embryo.site.panel.EmbryonicForm;

public class VoyagePlanForm extends EmbryonicForm<VoyagePlanForm> {

    private static final long serialVersionUID = 1L;

    private static final String JS_INIT = "embryo.voyagePlanForm.init('#id');";
    private final String js_init;

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

    private CompoundPropertyModel<VoyagePlan> model = new CompoundPropertyModel<>(new VoyagePlanModel());

    public VoyagePlanForm(String id) {
        super(id, "Voyage Plan");

        modalBody = new WebMarkupContainer("modalBody", model);
        add(modalBody);

        js_init = JS_INIT.replaceAll("id", modalBody.getMarkupId());

        feedback = new FeedbackPanel("voyage_plan_feedback", new ContainerFeedbackMessageFilter(this));
        feedback.setOutputMarkupId(true);
        feedback.setVisible(false);

        add(feedback);

        initializeListView(modalBody);

        saveLink = new AjaxSubmitLink("save") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                AjaxCallListener listener = new AjaxCallListener();
                listener.onBefore("embryo.voyagePlanForm.prepareRequest('#" + modalBody.getMarkupId() + "')");
                attributes.getAjaxCallListeners().add(listener);
            }

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                VoyagePlan info = model.getObject();
                // HACK an empty voyage was added, when loaded to generate empty row. Remove again before saving
                info.removeLastVoyage();
                shipService.saveVoyagePlan(info);
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

    @Override
    protected void onError() {
        // DOES NOT WORK, BECAUSE MODEL RELOADED?
        // updateFormComponentModels();

        super.onError();
    }

    private void initializeListView(WebMarkupContainer modalBody) {
        lv = new DynamicPropertyListView<Voyage>("voyagePlan") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<Voyage> item) {
                item.add(new HiddenField<String>("enavId"));
                item.add(new TypeaheadTextField<String, BerthDatum>("berthName", new BerthTypeaheadDataSource())
                        .autoInitialize(false));
                item.add(new LatitudeTextField("position.latitude"));
                item.add(new LongitudeTextField("position.longitude"));

                item.add(DateTimeTextField.forDateStyle("arrival", true, StyleDateConverter.DEFAULT_DATE_TIME));
                item.add(DateTimeTextField.forDateStyle("departure", true, StyleDateConverter.DEFAULT_DATE_TIME));

                // With Wicket 6.7.0
                item.add(new TextField<>("personsOnBoard").add(new RangeValidator<Integer>(1, 10000)));
                // With Wicket 1.5.8
                // personsOnboard.setRequired(true).add(new MinimumValidator<Integer>(1)).add(new
                // MaximumValidator<Integer>(10000));
                item.add(new CheckBox("doctorOnBoard"));

                WebMarkupContainer c = new WebMarkupContainer("route.enavId");
                item.add(c);
                if(item.getModelObject().getRoute() != null){
                    logger.debug("setting data-routeId={}", item.getModelObject().getRoute().getEnavId());
                    c.add(new AttributeAppender("data-routeId", item.getModelObject().getRoute().getEnavId()));
                }
            }
        };
        lv.setReuseItems(true);

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
        model.setChainedModel(new EmptyVoyagePlanModel());
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

    public class EmptyVoyagePlanModel extends Model<VoyagePlan> {
        private static final long serialVersionUID = -8287432934320097962L;

        public EmptyVoyagePlanModel() {
            IRequestParameters parameters = RequestCycle.get().getRequest().getRequestParameters();
            if (parameters.getParameterNames().contains("voyageCount")) {
                // update request being treated.
                // create empty voyage information objects as of no value to load them from database. They will be
                // overwritten later on in any case.
                Integer count = parameters.getParameterValue("voyageCount").toInteger();
                Ship2 ship = shipService.getYourShip();
                VoyagePlan info = new VoyagePlan();
                ship.setVoyagePlan(info);
                for (int i = 0; i < count; i++) {
                    info.addVoyageEntry(new Voyage());
                }
                setObject(info);
            } else {
                throw new IllegalStateException("request parameter voyageCount missing");
            }
        }
    }

    public class VoyagePlanModel extends LoadableDetachableModel<VoyagePlan> {

        private static final long serialVersionUID = 5624306986147386672L;

        @Override
        protected VoyagePlan load() {
            // Initial request. Load voyage information from database.
            Ship2 ship = shipService.getYourShip();
            VoyagePlan info = shipService.getVoyagePlan(ship.getMmsi());
            // HACK: Add empty voyage to generate empty row in case the user creates new voyages
            info.addVoyageEntry(new Voyage(null));
            return info;
        }
    }

    public static final class BerthTransformerFunction implements Function<Berth, BerthDatum> {
        private String value(final Berth input) {
            return input.getName() + (input.getAlias() != null ? " (" + input.getAlias() + ")" : "");
        }

        private String[] tokens(final Berth input) {
            if (input.getAlias() != null) {
                return new String[] { input.getName(), input.getAlias() };
            }
            return new String[] { input.getName() };
        }

        @Override
        public BerthDatum apply(final Berth input) {
            return new BerthDatum(value(input), tokens(input), input.getPosition().getLatitudeAsString(), input
                    .getPosition().getLongitudeAsString());
        }
    }

    public static class BerthDatum extends TypeaheadDatum {
        private String latitude;
        private String longitude;

        public BerthDatum(String value, String[] tokens, String latitude, String longitude) {
            super(value, tokens);
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getLatitude() {
            return latitude;
        }

        public String getLongitude() {
            return longitude;
        }
    }

    public class BerthTypeaheadDataSource implements TypeaheadDataSource<BerthDatum> {
        private static final long serialVersionUID = 565818402802493124L;

        @Override
        public List<BerthDatum> remoteFetch(String query) {
            logger.trace("remoteFetch({})", query);

            List<Berth> berths = geoService.findBerths(query);
            List<BerthDatum> transformed = Lists.transform(berths, new BerthTransformerFunction());

            logger.trace("berths={}", transformed);

            return transformed;
        }

        @Override
        public List<BerthDatum> prefetch() {
            logger.trace("prefetch()");

            List<Berth> berths = geoService.findBerths("");
            List<BerthDatum> transformed = Lists.transform(berths, new BerthTransformerFunction());

            logger.trace("berths={}", transformed);

            return transformed;
        }
    }
}
