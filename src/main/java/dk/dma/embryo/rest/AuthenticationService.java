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
package dk.dma.embryo.rest;

import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

import dk.dma.arcticweb.dao.RealmDao;
import dk.dma.embryo.domain.Permission;
import dk.dma.embryo.domain.Sailor;
import dk.dma.embryo.domain.SecuredUser;
import dk.dma.embryo.security.Subject;

@Path("/authentication")
public class AuthenticationService {
    @Inject
    private Subject subject;

    @Inject
    private RealmDao realmRepository;

    @Inject
    private Logger logger;

    @GET
    @Path("/details")
    @Produces("application/json")
    public Details details() {
        SecuredUser user = subject.getUser();
        if (user == null) {
            throw new UserNotAuthenticated();
        }

        Details details = new Details();

        if (subject.hasRole(Sailor.class)) {
            Sailor sailor = realmRepository.getSailor(subject.getUserId());
            details.setShipMmsi("" + sailor.getShip().getMmsi());
        }

        Set<Permission> perms = user.getPermissions();
        String[] permissions = new String[perms.size()];
        int count = 0;
        for(Permission permission : perms){
            permissions[count++] = permission.getLogicalName(); 
        }
                
        details.setProjection("EPSG:900913");
        details.setUserName(user.getUserName());
        details.setPermissions(permissions);
        
        return details;
    }

    @GET
    @Path("/logout")
    @Produces("application/json")
    public void logout() {
        subject.logout();
    }

    @GET
    @Path("/login")
    @Produces("application/json")
    public Details login(@QueryParam("userName") String userName, @QueryParam("password") String password) {
        SecuredUser user = subject.login(userName, password);

        logger.info("User "+userName+" : "+password+" -> "+user);

        if (user != null) {
            return details();
        } else {
            throw new UserNotAuthenticated();
        }
    }

    public static class UserNotAuthenticated extends WebApplicationException {
        private static final long serialVersionUID = 7940360206022406100L;

        public UserNotAuthenticated() {
            super(Response.Status.UNAUTHORIZED);
        }
    }

    public static class Details {
        private String shipMmsi;
        private String projection;
        private String userName;
        private String[] permissions;

        public String getShipMmsi() {
            return shipMmsi;
        }

        public void setShipMmsi(String shipMmsi) {
            this.shipMmsi = shipMmsi;
        }

        public String getProjection() {
            return projection;
        }

        public void setProjection(String projection) {
            this.projection = projection;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String[] getPermissions() {
            return permissions;
        }

        public void setPermissions(String[] permissions) {
            this.permissions = permissions;
        }
    }
}
