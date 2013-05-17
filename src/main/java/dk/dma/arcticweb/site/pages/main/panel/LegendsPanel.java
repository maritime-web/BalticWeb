package dk.dma.arcticweb.site.pages.main.panel;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
public class LegendsPanel extends Panel {
	
	private final static String JS_INIT = "embryo.legendsPanel.init();";

	public LegendsPanel(String id) {
		super(id);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		// if component is disabled we don't have to load the JQueryUI datepicker
		if (!isEnabledInHierarchy())
			return;
		// initialize component
		response.render(OnLoadHeaderItem.forScript(JS_INIT));
	}
	
	

}
