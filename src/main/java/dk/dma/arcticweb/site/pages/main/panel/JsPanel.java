package dk.dma.arcticweb.site.pages.main.panel;

import javax.ejb.EJB;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import dk.dma.arcticweb.domain.Ship;
import dk.dma.arcticweb.service.StakeholderService;
import dk.dma.arcticweb.site.session.ArcticWebSession;

public class JsPanel extends Panel {
	
	private static final long serialVersionUID = 1L;
	
	@EJB
	StakeholderService stakeholderService;

	public JsPanel(String id) {
		super(id);
		setRenderBodyOnly(true);

		// Get stakeholder type and possibly ship MMSI
		ArcticWebSession session = ArcticWebSession.get();		
		String stakeholderType = session.getStakeholder().getStakeholderType();		
		String shipMmsi = "null";
		if (session.getStakeholder() instanceof Ship) {
			shipMmsi = Long.toString(((Ship)session.getStakeholder()).getMmsi());
		}

		// Make label
		StringBuilder js = new StringBuilder();
		js.append("var stakeholder_type = '" + stakeholderType + "';\n");
		js.append("var ship_mmsi = " + shipMmsi + ";\n");
		add(new Label("js", "\n" + js.toString()).setEscapeModelStrings(false));
	}

}
