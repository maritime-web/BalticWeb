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
package dk.dma.arcticweb.site.session;

import java.util.Locale;

import javax.ejb.EJB;

import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;

import dk.dma.arcticweb.service.UserService;
import dk.dma.embryo.domain.User;

/**
 * Session object
 */
public class ArcticWebSession extends WebSession {

    @EJB
    private static UserService userService;

    private User user;

    private static final long serialVersionUID = 1L;

    public static ArcticWebSession get() {
        return (ArcticWebSession) Session.get();
    }

    public ArcticWebSession(Request request) {
        super(request);
        setLocale(Locale.US);
    }

//    public void loginUser(User user) {
//        this.user = user;
//        this.stakeholder = stakeholderService.getStakeholder(user);
//    }
//
//    public boolean isLoggedIn() {
//        return user != null;
//    }
//
//    public void logout() {
//        this.user = null;
//    }
//
//    public User getUser() {
//        return user;
//    }
//
//    public Stakeholder getStakeholder() {
//        return stakeholder;
//    }

}
