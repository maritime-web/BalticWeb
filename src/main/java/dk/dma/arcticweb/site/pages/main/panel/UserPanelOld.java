/* Copyright (c) 2011 Danish Maritime Authority
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
package dk.dma.arcticweb.site.pages.main.panel;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import dk.dma.arcticweb.site.pages.front.FrontPage;
import dk.dma.arcticweb.site.session.ArcticWebSession;

public class UserPanelOld extends Panel {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(UserPanel.class);

    private WebMarkupContainer notLoggedIn;
    private WebMarkupContainer loggedIn;

    public UserPanelOld(String id) {
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
