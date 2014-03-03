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

import java.util.ArrayList;
import java.util.List;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import dk.dma.embryo.domain.Role;
import dk.dma.embryo.domain.SailorRole;
import dk.dma.embryo.domain.SecuredUser;
import dk.dma.embryo.security.Subject;
import dk.dma.embryo.service.UserService;

@Path("/user")
public class UserRestService {

    @Inject
    private Logger logger;

    @Inject
    private Subject subject;
    
    @Inject
    private UserService userService;

    @DELETE
    @Path("/delete/{login}")
    @Produces("application/json")
    public void delete(@PathParam("login") String login) {
        logger.info("Deleting " + login);
        userService.delete(login);
    }

    @PUT
    @Path("/create")
    @Consumes("application/json")
    public void create(User user) {
        logger.info("Creating new user " + user.getLogin() + "  in role " + user.getRole());
        userService.create(user.getLogin(), user.getPassword(), user.getShipMmsi(), user.getEmail(), user.getRole());
    }

    @PUT
    @Path("/edit")
    @Consumes("application/json")
    public void edit(User user) {
        logger.info("Editing new user " + user.getLogin() + "  in role " + user.getRole());
        userService.edit(user.getLogin(), user.getShipMmsi(), user.getEmail(), user.getRole());
    }

    
    @GET
    @GZIP
    @Path("/list")
    @Produces("application/json")
    @NoCache
    public List<User> list() {
        List<User> result = new ArrayList<>();

        for (SecuredUser su : userService.list()) {
            User user = new User();

            user.setLogin(su.getUserName());
            user.setEmail(su.getEmail());
            Role role = su.getRole();

            user.setRole(role == null ? null : role.getLogicalName());
            if (role instanceof SailorRole) {
                SailorRole sailor = (SailorRole)role;
                user.setShipMmsi(sailor.getVessel().getMmsi());
            }

            result.add(user);
        }

        return result;
    }

    public static class User {
        private String login;
        private String password;
        private String role;
        private Long shipMmsi;
        private String email;

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public Long getShipMmsi() {
            return shipMmsi;
        }

        public void setShipMmsi(Long shipMmsi) {
            this.shipMmsi = shipMmsi;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
