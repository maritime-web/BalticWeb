package dk.dma.arcticweb.site.pages.main.panel;

import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
public class LeftPanel extends Panel {

	public LeftPanel(String id) {
		super(id);		
		add(new LegendsPanel("legends"));
		add(new SearchPanel("search"));
		add(new VesselDetailsPanel("vesselDetails"));
	}
	
}
