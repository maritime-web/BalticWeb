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
package dk.dma.embryo.service;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.slf4j.Logger;

import dk.dma.embryo.dao.RealmDao;
import dk.dma.embryo.dao.VesselDao;
import dk.dma.embryo.domain.AdministratorRole;
import dk.dma.embryo.domain.ReportingAuthorityRole;
import dk.dma.embryo.domain.Role;
import dk.dma.embryo.domain.SailorRole;
import dk.dma.embryo.domain.SecuredUser;
import dk.dma.embryo.domain.ShoreRole;
import dk.dma.embryo.domain.Vessel;
import dk.dma.embryo.security.AuthorizationChecker;
import dk.dma.embryo.security.SecurityUtil;
import dk.dma.embryo.security.authorization.Roles;

@Stateless
@Interceptors(value = AuthorizationChecker.class)
@Roles(AdministratorRole.class)
public class UserServiceImpl implements UserService {

    @Inject
    private RealmDao realmDao;

    @Inject
    private VesselDao vesselDao;

    @Inject
    private Logger logger;

    private Role createRole(String role, Long mmsi) {
        switch (role) {
        case "Sailor":
            Vessel vessel = new Vessel();
            vessel.setMmsi(mmsi);
            vesselDao.saveEntity(vessel);

            SailorRole sailor = new SailorRole();
            sailor.setVessel(vessel);
            realmDao.saveEntity(sailor);
            return sailor;
        case "Shore":
            ShoreRole shore = new ShoreRole();
            realmDao.saveEntity(shore);
            return shore;
        case "Reporting":
            ReportingAuthorityRole authority = new ReportingAuthorityRole();
            realmDao.saveEntity(authority);
            return authority;
        case "Administration":
            AdministratorRole administator = new AdministratorRole();
            realmDao.saveEntity(administator);
            return administator;
        }
        return null;
    }

    @Override
    public void create(String login, String password, Long mmsi, String email, String role) {
        SecuredUser su = SecurityUtil.createUser(login, password, email);
        su.setRole(createRole(role, mmsi));
        realmDao.saveEntity(su);
    }

    public void edit(String login, Long mmsi, String email, String role){
            SecuredUser user = realmDao.findByUsername(login);
            
            user.setEmail(email);
            
            if(user.getRole() != null && !user.getRole().getLogicalName().equalsIgnoreCase(role)){
                if(user.getRole().getClass() == SailorRole.class){
                    Role oldRole = user.getRole();
                    Vessel oldVessel = ((SailorRole)oldRole).getVessel();
                    user.setRole(null);
                    realmDao.saveEntity(user);
                    realmDao.remove(oldRole);
                    vesselDao.remove(oldVessel);
                }else if(user.getRole() != null){
                    Role oldRole = user.getRole();
                    user.setRole(null);
                    realmDao.saveEntity(user);
                    realmDao.remove(oldRole);
                }
            }

            user.setRole(createRole(role, mmsi));
            realmDao.saveEntity(user);
    }

    @Override
    public void delete(String login) {
        realmDao.remove(realmDao.findByUsername(login));
    }

    @Override
    public List<SecuredUser> list() {
        return realmDao.list();
    }
}
