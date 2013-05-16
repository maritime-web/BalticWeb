package dk.dma.arcticweb.site.pages.main.panel;

import org.apache.wicket.markup.html.panel.Panel;

import dk.dma.arcticweb.site.pages.main.form.VoyageInformationForm;

public class VoyageInformationPanel extends Panel {
	
	private static final long serialVersionUID = 1L;
	
	public VoyageInformationPanel(String id) {
		super(id);
		
		add(new VoyageInformationForm("voyage_information_form"));
	}
	

}
