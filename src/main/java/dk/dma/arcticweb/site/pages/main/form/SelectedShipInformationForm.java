package dk.dma.arcticweb.site.pages.main.form;

import java.util.List;

import javax.ejb.EJB;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.validation.validator.MaximumValidator;
import org.apache.wicket.validation.validator.MinimumValidator;
import org.apache.wicket.validation.validator.StringValidator;

import dk.dma.arcticweb.service.StakeholderService;
import dk.dma.enav.model.ship.ShipType;

public class SelectedShipInformationForm extends Form<SelectedShipInformationForm> {

	private static final long serialVersionUID = 1L;

	@EJB
	StakeholderService stakeholderService;

	private TextField<Long> mmsi;
	private TextField<String> name;
	private TextField<Long> imoNo;
	private TextField<String> callsign;
	private DropDownChoice<String> type;
	private TextField<Integer> maxSpeed;
	private TextField<Integer> tonnage;
	private TextField<String> commCapabilities;
	private TextField<Integer> rescueCapacity;
	private TextField<Integer> width;
	private TextField<Integer> length;
	private TextField<String> iceClass;
	private CheckBox helipad;
	
	private FeedbackPanel feedback;
	private WebMarkupContainer saved;

	public SelectedShipInformationForm(String id) {
		super(id);

		mmsi = new TextField<>("mmsi");
		mmsi.setRequired(true).add(new MinimumValidator<Long>(100000000L)).add(new MaximumValidator<Long>(999999999L));
		name = new TextField<>("name");
		name.setRequired(true);
		imoNo = new TextField<>("imoNo");
		callsign = new TextField<>("callsign");
		callsign.add(StringValidator.maximumLength(32));
		List<String> types = ShipType.getStringList();
		type = new DropDownChoice<>("type", types);
		maxSpeed = new TextField<>("maxSpeed");
		maxSpeed.add(new MaximumValidator<Integer>(200));
		tonnage = new TextField<>("tonnage");
		commCapabilities = new TextField<>("commCapabilities");
		rescueCapacity = new TextField<>("rescueCapacity");
		width = new TextField<>("width");
		length = new TextField<>("length");
		iceClass = new TextField<>("iceClass");
		helipad = new CheckBox("helipad");
		
		
		feedback = new FeedbackPanel("ship_information_feedback");
		feedback.setVisible(false);
		saved = new WebMarkupContainer("saved");
		saved.setVisible(false);

		add(mmsi);
		add(name);
		add(imoNo);
		add(callsign);	
		add(type);
		add(maxSpeed);
		add(tonnage);
		add(commCapabilities);
		add(rescueCapacity);
		add(width);
		add(length);
		add(iceClass);
		add(helipad);
		
		add(feedback);
		add(saved);
	}

}
