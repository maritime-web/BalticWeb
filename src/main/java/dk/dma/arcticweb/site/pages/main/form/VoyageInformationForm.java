package dk.dma.arcticweb.site.pages.main.form;

import javax.ejb.EJB;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.validation.validator.MaximumValidator;
import org.apache.wicket.validation.validator.MinimumValidator;

import dk.dma.arcticweb.domain.Ship;
import dk.dma.arcticweb.domain.VoyageInformation;
import dk.dma.arcticweb.service.StakeholderService;
import dk.dma.arcticweb.site.pages.main.MainPage;
import dk.dma.arcticweb.site.session.ArcticWebSession;

public class VoyageInformationForm extends Form<VoyageInformationForm> {
	
	private static final long serialVersionUID = 1L;
	
	@EJB
	StakeholderService stakeholderService;
	
	private TextField<Integer> personsOnboard;
	private CheckBox doctorOnboard;
	
	private FeedbackPanel feedback;
	private AjaxSubmitLink saveLink;
	
	private VoyageInformation voyageInformation;
	
	public VoyageInformationForm(String id) {
		super(id);
		final Ship ship = (Ship) ArcticWebSession.get().getStakeholder();
		voyageInformation = stakeholderService.getVoyageInformation(ship);
		
		setDefaultModel(new CompoundPropertyModel<VoyageInformation>(voyageInformation));
		
		personsOnboard = new TextField<>("personsOnboard");
		personsOnboard.setRequired(true).add(new MinimumValidator<Integer>(1)).add(new MaximumValidator<Integer>(10000));
		
		doctorOnboard = new CheckBox("doctorOnboard");
		
		feedback = new FeedbackPanel("voyage_information_feedback");
		feedback.setVisible(false);
		
		saveLink = new AjaxSubmitLink("save") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				stakeholderService.saveVoyageInformation(ship, voyageInformation);
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
