package dk.dma.arcticweb.site.pages.main.panel;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;

import dk.dma.arcticweb.domain.Stakeholder;
import dk.dma.arcticweb.site.session.ArcticWebSession;

public class MenuPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	private WebMarkupContainer yourShip;
	private WebMarkupContainer selectedShip;
	
	public MenuPanel(String id) {
		super(id);
		
		Stakeholder stakeholder = ArcticWebSession.get().getStakeholder();
		
		yourShip = new WebMarkupContainer("your_ship");
		yourShip.setVisible(stakeholder.isShip());
		
		add(yourShip);
		
		selectedShip = new WebMarkupContainer("selected_ship");
		selectedShip.setVisible(true);
		
		add(selectedShip);
		
	}

}
