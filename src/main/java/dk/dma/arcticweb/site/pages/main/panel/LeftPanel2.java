package dk.dma.arcticweb.site.pages.main.panel;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

@SuppressWarnings("serial")
public class LeftPanel2 extends Panel {

	private final RepeatingView rw;
	
	private final static String JS_INIT = "embryo.leftPanel.init();";

	public LeftPanel2(String id) {
		super(id);

		rw = new RepeatingView("collapseGroup");
		add(rw);

		String collapsableGroupId = rw.newChildId();
		add("Legends", collapsableGroupId, new LegendsPanel("collapsableContent"));

		collapsableGroupId = rw.newChildId();
		add("Search", collapsableGroupId, new SearchPanel("collapsableContent"));
//
		collapsableGroupId = rw.newChildId();
		add("Vessel details", collapsableGroupId, new VesselDetailsPanel("collapsableContent"));
	}

	private void add(String header, String collapsableGroupId, Component content) {
		WebMarkupContainer collapsableGroup = new WebMarkupContainer(collapsableGroupId);
		rw.add(collapsableGroup);

		WebMarkupContainer collapsable = new WebMarkupContainer("collapsable");
		// Get auto generated html id 
		String idSelector = "#" + collapsable.getMarkupId();
		collapsable.add(content);


		Label headerText = new Label("collapseHeader", header);
		headerText.add(new AttributeModifier("data-target", idSelector));
//		ExternalLink headerLink = new ExternalLink(, idSelector);
//		headerLink.add(new AttributeModifier("data-target", idSelector));

		collapsableGroup.add(collapsable);
		//collapsableGroup.add(headerLink);
		collapsableGroup.add(headerText);
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
