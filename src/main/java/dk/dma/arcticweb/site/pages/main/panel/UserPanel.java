package dk.dma.arcticweb.site.pages.main.panel;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import dk.dma.arcticweb.site.pages.front.FrontPage;
import dk.dma.arcticweb.site.session.ArcticWebSession;

public class UserPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	private static final Logger LOG = Logger.getLogger(UserPanel.class);
	
	private WebMarkupContainer notLoggedIn;
	private WebMarkupContainer loggedIn;

	public UserPanel(String id) {
		super(id);
		
		notLoggedIn = new WebMarkupContainer("not_logged_in");
		loggedIn = new WebMarkupContainer("logged_in");
		notLoggedIn.setVisible(false);
		loggedIn.setVisible(false);
				
		add(notLoggedIn);
		loggedIn.add(new Label("username", new PropertyModel<UserPanel>(this, "username")));
		add(loggedIn);
		
		Link<String> logoutLink = new Link<String>("logout_link") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick() {
				LOG.info("Logging out: " + getUsername());
				ArcticWebSession.get().logout();
				setResponsePage(new FrontPage());
			}
		};
		loggedIn.add(logoutLink);		
	}
	
	@Override
	protected void onConfigure() {		
		super.onConfigure();
		loggedIn.setVisible(ArcticWebSession.get().isLoggedIn());
		notLoggedIn.setVisible(!ArcticWebSession.get().isLoggedIn());
	}
	
	public String getUsername() {		
		if (ArcticWebSession.get().isLoggedIn()) {
			return ArcticWebSession.get().getUser().getUsername();
		}
		return null;
	}

}
