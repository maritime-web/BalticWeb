/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.embryo.user.json;

import java.util.ArrayList;
import java.util.List;

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

import dk.dma.embryo.user.model.Role;
import dk.dma.embryo.user.model.SailorRole;
import dk.dma.embryo.user.model.SecuredUser;
import dk.dma.embryo.user.service.UserService;

@Path("/user")
public class UserRestService {

    @Inject
    private Logger logger;

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
