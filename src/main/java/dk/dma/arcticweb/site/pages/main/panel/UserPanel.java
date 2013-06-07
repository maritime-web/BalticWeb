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

import javax.inject.Inject;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.slf4j.Logger;

import dk.dma.arcticweb.site.pages.front.FrontPage;
import dk.dma.embryo.security.Subject;

public class UserPanel extends Panel {

    private static final long serialVersionUID = 1L;

    @Inject
    private transient Logger logger;

    private WebMarkupContainer notLoggedIn;
    private WebMarkupContainer loggedIn;
    
    @Inject
    private Subject subject;

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
                logger.info("Logging out: " + getUsername());
                subject.logout();
                setResponsePage(new FrontPage());
            }
        };
        loggedIn.add(logoutLink);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        loggedIn.setVisible(subject.isLoggedIn());
        notLoggedIn.setVisible(!subject.isLoggedIn());
    }

    public String getUsername() {
        if (subject.isLoggedIn()) {
            return subject.getUser().getUserName();
        }
        return null;
    }

}
