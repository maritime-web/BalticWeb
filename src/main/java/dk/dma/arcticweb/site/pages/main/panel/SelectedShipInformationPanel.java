package dk.dma.arcticweb.site.pages.main.panel;

import org.apache.wicket.markup.html.panel.Panel;

import dk.dma.arcticweb.site.pages.main.form.SelectedShipInformationForm;

public class SelectedShipInformationPanel extends Panel {
	
	private static final long serialVersionUID = 1L;

	public SelectedShipInformationPanel(String id) {
		super(id);
		
		add(new SelectedShipInformationForm("selected_ship_information_form"));		
	}

}
