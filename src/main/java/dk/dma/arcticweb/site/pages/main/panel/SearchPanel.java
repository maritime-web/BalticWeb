package dk.dma.arcticweb.site.pages.main.panel;

import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
public class SearchPanel extends Panel {

	private final static String JS_INIT = "embryo.searchPanel.init();";

	public SearchPanel(String id) {
		super(id);
	}

//	@Override
//	public void renderHead(IHeaderResponse response) {
//		super.renderHead(response);
//		// if component is disabled we don't have to load the JQueryUI datepicker
//		if (!isEnabledInHierarchy())
//			return;
//		// initialize component
//		response.render(OnLoadHeaderItem.forScript(JS_INIT));
//	}
	
	
}
