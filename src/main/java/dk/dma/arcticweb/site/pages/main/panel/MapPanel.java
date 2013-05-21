package dk.dma.arcticweb.site.pages.main.panel;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
public class MapPanel extends Panel {

	private final static String JS_INIT = "embryo.mapPanel.init('projection');";
	
	public static final OnLoadHeaderItem MAP_INIT;

	static{
		String js_init = JS_INIT.replaceAll("projection", "EPSG:900913");
		MAP_INIT = OnLoadHeaderItem.forScript(js_init);
	}
	
	public MapPanel(String id) {
		super(id);
		
		add(new LeftPanel("left"));
		add(new StatusPanel("status"));
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		// initialize component
		response.render(MAP_INIT);
	}
	

}
