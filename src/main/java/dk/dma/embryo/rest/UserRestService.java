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
import dk.dma.arcticweb.dao.VesselDao;
import dk.dma.embryo.domain.AuthorityRole;
import dk.dma.embryo.domain.Permission;
import dk.dma.embryo.domain.Role;
import dk.dma.embryo.domain.Sailor;
import dk.dma.embryo.domain.SecuredUser;
import dk.dma.embryo.domain.ShoreRole;
import dk.dma.embryo.domain.Vessel;
import org.slf4j.Logger;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Path("/user")
public class UserRestService {
    @Inject
    private VesselDao vesselDao;

    @Inject
    private RealmDao realmDao;

    @Inject
    private Logger logger;

    @GET
    @Path("/delete")
    @Produces("application/json")
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void delete(@QueryParam("login") String login) {
        logger.info("Deleting " + login);
        vesselDao.remove(realmDao.findByUsername(login));
    }

    @POST
    @Path("/save")
    @Consumes("application/json")
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void save(User user) {
        logger.info("Creating new user " + user.getLogin() + "  in role " + user.getRole());

        SecuredUser su = new SecuredUser(user.getLogin(), user.getPassword(), "obo@dma.dk");

        switch (user.getRole()) {
            case "Sailor":
                Permission ais = new Permission("ais");
                Permission yourShip = new Permission("yourShip");

                vesselDao.saveEntity(ais);
                vesselDao.saveEntity(yourShip);

                Vessel vessel = new Vessel();

                vessel.setMmsi(user.getShipMmsi());
                vesselDao.saveEntity(vessel);

                Sailor sailor = new Sailor();

                sailor.add(ais);
                sailor.add(yourShip);
                sailor.setVessel(vessel);
                vesselDao.saveEntity(sailor);

                su.addRole(sailor);
                break;
            case "Shore":
                ShoreRole shore = new ShoreRole();
                vesselDao.saveEntity(shore);

                su.addRole(shore);
                break;
            case "Greenpos Authority":
                Permission greenposList = new Permission("GreenposList");
                vesselDao.saveEntity(greenposList);

                ShoreRole authority = new ShoreRole();
                authority.add(greenposList);
                vesselDao.saveEntity(authority);

                su.addRole(authority);
                break;
            case "Administrator":
                Permission administration = new Permission("Administration");
                vesselDao.saveEntity(administration);

                ShoreRole administator = new ShoreRole();
                administator.add(administration);
                vesselDao.saveEntity(administator);

                su.addRole(administator);
                break;
        }

        vesselDao.saveEntity(su);
    }

    @Inject
    protected EntityManager em;

    @GET
    @Path("/list")
    @Produces("application/json")
    public List<User> list() {
        List<User> result = new ArrayList<>();

        for (SecuredUser su : realmDao.getAll(SecuredUser.class)) {
            User user = new User();

            su = realmDao.getByPrimaryKeyReturnAll(su.getId());

            user.setLogin(su.getUserName());
            Role role = su.getRoles().iterator().next();

            Set<String> permissions = new HashSet<>();

            for (Permission p : su.getPermissions()) {
                permissions.add(p.getLogicalName());
            }

            if (role instanceof Sailor) {
                user.setRole("Sailor");
                Sailor sailor = realmDao.getSailor(su.getId());
                user.setShipMmsi(sailor.getVessel().getMmsi());
            } else if (role instanceof ShoreRole || role instanceof AuthorityRole) {
                if (permissions.contains("Administration")) {
                    user.setRole("Administrator");
                } else if (permissions.contains("GreenposList")) {
                    user.setRole("Greenpos Administrator");
                } else {
                    user.setRole("Shore");
                }
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
    }
}
