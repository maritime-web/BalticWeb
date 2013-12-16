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

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import dk.dma.arcticweb.dao.RealmDao;
import dk.dma.arcticweb.service.EmbryoLogService;
import dk.dma.configuration.Property;
import dk.dma.embryo.domain.SailorRole;
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

    @Inject
    private EmbryoLogService embryoLogService;

    @Inject
    @Property("embryo.osm.url")
    private String osm;

    @GET
    @Path("/details")
    @Produces("application/json")
    @GZIP
    @NoCache
    public Details details() {
        SecuredUser user = subject.getUser();
        if (user == null) {
            throw new UserNotAuthenticated();
        }

        Details details = new Details();

        if (subject.hasRole(SailorRole.class)) {
            SailorRole sailor = realmRepository.getSailor(subject.getUserId());
            details.setShipMmsi("" + sailor.getVessel().getMmsi());
        }

        String[] rolesJson = new String[]{user.getRole().getLogicalName()};
        details.setProjection("EPSG:900913");
        details.setUserName(user.getUserName());
        details.setPermissions(rolesJson);
        details.setOsm(osm);

        logger.debug("details() : {}", details);
        return details;
    }

    @GET
    @Path("/logout")
    @Produces("application/json")
    @GZIP
    @NoCache
    public void logout() {
        if (subject != null && subject.getUser() != null) {
            logger.debug("User {} logged out", subject.getUser().getUserName());
            embryoLogService.info("User " + subject.getUser().getUserName() + " logged out");
        } else {
            logger.error("Attempt to logout all though not logged in");
            embryoLogService.error("Attempt to logout all though not logged in");
        }
        subject.logout();
    }

    @GET
    @Path("/login")
    @Produces("application/json")
    @GZIP
    @NoCache
    public Details login(@QueryParam("userName") String userName, @QueryParam("password") String password) {
        try {
            SecuredUser user = subject.login(userName, password);

            if (user != null) {
                logger.debug("User {} logged in", userName);
                embryoLogService.info("User " + userName + " logged in");
                return details();
            } else {
                logger.debug("User {} not logged in (wrong username / password)", userName);
                embryoLogService.info("User " + userName + " not logged in (wrong username / password)");
                throw new UserNotAuthenticated();
            }
        } catch (org.apache.shiro.authc.IncorrectCredentialsException e) {
            logger.debug("User {} not logged in (wrong username / password)", userName);
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
        private String osm;

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

        public String getOsm() {
            return osm;
        }

        public void setOsm(String osm) {
            this.osm = osm;
        }

        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this);
        }
    }
}
