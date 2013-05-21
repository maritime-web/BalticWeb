package dk.dma.arcticweb.site.resources;

import java.util.Arrays;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;

import dk.dma.arcticweb.site.pages.main.panel.MapPanel;


public class OnLoadMapDependentHeaderItem extends OnLoadHeaderItem {

	public OnLoadMapDependentHeaderItem(CharSequence javaScript) {
		super(javaScript);
	}
	
	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Arrays.asList(MapPanel.MAP_INIT);
	}

	public static OnLoadMapDependentHeaderItem forScript(CharSequence javaScript){
		return new OnLoadMapDependentHeaderItem(javaScript);
	}

}
