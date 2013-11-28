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

import dk.dma.arcticweb.dao.RealmDao;
import dk.dma.arcticweb.service.EmbryoLogService;
import dk.dma.embryo.domain.Permission;
import dk.dma.embryo.domain.Sailor;
import dk.dma.embryo.domain.SecuredUser;
import dk.dma.embryo.security.Subject;
import org.jboss.resteasy.annotations.GZIP;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Set;

@Path("/authentication")
public class AuthenticationService {
    @Inject
    private Subject subject;

    @Inject
    private RealmDao realmRepository;

    @Inject
    private Logger logger;

    @Inject
    private EmbryoLogService embryoLogService;

    @GET
    @Path("/details")
    @Produces("application/json")
    @GZIP
    public Details details() {
        SecuredUser user = subject.getUser();
        if (user == null) {
            throw new UserNotAuthenticated();
        }

        Details details = new Details();

        if (subject.hasRole(Sailor.class)) {
            Sailor sailor = realmRepository.getSailor(subject.getUserId());
            details.setShipMmsi("" + sailor.getVessel().getMmsi());
        }

        Set<Permission> perms = user.getPermissions();
        String[] permissions = new String[perms.size()];
        int count = 0;
        for (Permission permission : perms) {
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
    @GZIP
    public void logout() {
        embryoLogService.info("User " + subject.getUser().getUserName() + " logged out");
        subject.logout();
    }

    @GET
    @Path("/login")
    @Produces("application/json")
    @GZIP
    public Details login(@QueryParam("userName") String userName, @QueryParam("password") String password) {
        try {
            SecuredUser user = subject.login(userName, password);

            if (user != null) {
                embryoLogService.info("User " + userName + " logged in");
                return details();
            } else {
                embryoLogService.info("User " + userName + " not logged in (wrong username / password)");
                throw new UserNotAuthenticated();
            }
        } catch (org.apache.shiro.authc.IncorrectCredentialsException e) {
            embryoLogService.info("User " + userName + " not logged in (wrong username / password)");
            throw e;
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
