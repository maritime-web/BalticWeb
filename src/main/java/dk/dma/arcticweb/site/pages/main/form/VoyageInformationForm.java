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
package dk.dma.arcticweb.site.pages.main.form;

import javax.inject.Inject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.validation.validator.RangeValidator;

import dk.dma.arcticweb.service.ShipService;
import dk.dma.arcticweb.site.pages.main.MainPage;
import dk.dma.embryo.domain.VoyageInformation2;
import dk.dma.embryo.site.panel.EmbryonicForm;

public class VoyageInformationForm extends EmbryonicForm<VoyageInformationForm> {

    private static final long serialVersionUID = 1L;


    private TextField<Integer> personsOnboard;
    private CheckBox doctorOnboard;

    private FeedbackPanel feedback;
    private AjaxSubmitLink saveLink;

    private VoyageInformation2 voyageInformation;
    
    @Inject
    private ShipService shipService;

    public VoyageInformationForm(String id) {
        super(id, "Voyage Information");
        
        voyageInformation = shipService.getYourShip().getVoyageInformation();

        setDefaultModel(new CompoundPropertyModel<VoyageInformation2>(voyageInformation));

        personsOnboard = new TextField<>("personsOnboard");
        // With Wicket 6.7.0
        personsOnboard.add(new RangeValidator<Integer>(1, 10000));
        // With Wicket 1.5.8
        // personsOnboard.setRequired(true).add(new MinimumValidator<Integer>(1)).add(new   
        // MaximumValidator<Integer>(10000));

        doctorOnboard = new CheckBox("doctorOnboard");

        feedback = new FeedbackPanel("voyage_information_feedback");
        feedback.setVisible(false);

        saveLink = new AjaxSubmitLink("save") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                shipService.saveVoyageInformation(voyageInformation);
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

        add(personsOnboard);
        add(doctorOnboard);
        add(feedback);
        add(saveLink);
    }

}
