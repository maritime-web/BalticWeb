package dk.dma.arcticweb.site.pages.main.panel;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;

import dk.dma.arcticweb.site.resources.OnLoadMapDependentHeaderItem;

@SuppressWarnings("serial")
public class StatusPanel extends Panel {

	private final static String JS_INIT = "embryo.statusPanel.init('projection');";

	public StatusPanel(String id) {
		super(id);		
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		// if component is disabled we don't have to load the JQueryUI datepicker
		if (!isEnabledInHierarchy())
			return;
		// initialize component
		String js_init = JS_INIT.replaceAll("projection", "EPSG:4326");
		response.render(OnLoadMapDependentHeaderItem.forScript(js_init));
	}
	

}
