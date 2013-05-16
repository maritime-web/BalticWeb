/* Copyright (c) 2012 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.arcticweb.site.pages.front;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

import dk.dma.arcticweb.site.pages.BasePage;
import dk.dma.arcticweb.site.pages.main.MainPage;
import dk.dma.arcticweb.site.pages.main.panel.UserPanel;
import dk.dma.arcticweb.site.session.ArcticWebSession;

public class FrontPage extends BasePage {
	private static final long serialVersionUID = 1L;
	
	private WebMarkupContainer viewerMenu;
	
	public FrontPage() {
		super();
		
		LoginForm loginForm = new LoginForm("login_form");
		loginForm.setOutputMarkupId(true);
		add(loginForm);
		
		add(new UserPanel("user_panel"));
		viewerMenu = new WebMarkupContainer("viewer_menu");
		viewerMenu.add(new BookmarkablePageLink<>("viewer_link", MainPage.class));
		viewerMenu.setVisible(false);
		add(viewerMenu);		
	}
	
	@Override
	protected void onConfigure() {
		super.onConfigure();
		if (viewerMenu != null) {
			viewerMenu.setVisible(ArcticWebSession.get().isLoggedIn());
		}
	}
	
}
