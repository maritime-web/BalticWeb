package dk.dma.arcticweb.site.pages.main.panel;

import org.apache.wicket.markup.html.panel.Panel;

import dk.dma.arcticweb.site.pages.main.form.ShipInformationForm;

public class ShipInformationPanel extends Panel {
	
	private static final long serialVersionUID = 1L;

	public ShipInformationPanel(String id) {
		super(id);
		
		add(new ShipInformationForm("ship_information_form"));		
	}

}
