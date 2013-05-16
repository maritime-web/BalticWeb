package dk.dma.arcticweb.site.pages.main.panel;

import org.apache.wicket.markup.html.panel.Panel;

import dk.dma.arcticweb.site.pages.main.form.ShipReportForm;

public class ShipReportPanel extends Panel {
	
	private static final long serialVersionUID = 1L;

	public ShipReportPanel(String id) {
		super(id);
		
		add(new ShipReportForm("ship_report_form"));		
	}

}
