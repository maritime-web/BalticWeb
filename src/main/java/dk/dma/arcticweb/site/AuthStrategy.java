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
package dk.dma.arcticweb.site;

import org.apache.wicket.authorization.strategies.page.SimplePageAuthorizationStrategy;

import dk.dma.arcticweb.site.pages.FrontPage;
import dk.dma.embryo.config.Configuration;
import dk.dma.embryo.security.Subject;

/**
 * Simple authentication strategy
 */
public class AuthStrategy extends SimplePageAuthorizationStrategy {

    public AuthStrategy() {
        super(SecurePage.class, FrontPage.class);
        
    }

    // @Inject
    // private Subject subject;

    @Override
    protected boolean isAuthorized() {
        // Wicket CDI does not allow injection into IAuthorizationStrategy. Manual retrieval required. 
        Subject subject = Configuration.getBean(Subject.class);
        return subject.isLoggedIn();
        // return ArcticWebSession.get().isLoggedIn();
    }

}
