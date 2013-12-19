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
package dk.dma.embryo.security;

import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.slf4j.Logger;

import dk.dma.embryo.dao.RealmDao;
import dk.dma.embryo.dao.ScheduleDao;
import dk.dma.embryo.domain.Role;
import dk.dma.embryo.domain.Route;
import dk.dma.embryo.domain.SailorRole;
import dk.dma.embryo.domain.SecuredUser;

/**
 * Subject class wrapping all access to shiro security and also decorating with extra syntactic sugar.
 * 
 * 
 * @author Jesper Tejlgaard
 * 
 */
@SessionScoped
public class SubjectImpl implements Subject {

    private static final long serialVersionUID = -7771436245663646148L;

    @Inject
    private transient RealmDao realmDao;

    @Inject
    private transient ScheduleDao scheduleDao;

    @Inject
    private transient Logger logger;

    public SecuredUser login(String userName, String password, Boolean rememberMe) {
        // collect user principals and credentials in a gui specific manner
        // such as username/password html form, X509 certificate, OpenID, etc.
        // We'll use the username/password example here since it is the most common.
        // (do you know what movie this is from? ;)
        UsernamePasswordToken token = new UsernamePasswordToken(userName, password, rememberMe);
        // this is all you have to do to support 'remember me' (no config - built in!):
        // token.setRememberMe(true);
        SecurityUtils.getSubject().login(token);

        return realmDao.findByUsername(userName);
    }

    public SecuredUser login(String userName, String password) {
        return login(userName, password, Boolean.FALSE);
    }


    /**
     * Expected used while transitioning from role base security to feature base security
     * 
     * @param permission
     * @return
     */
    public <R extends Role> boolean hasRole(Class<R> roleType) {
        try {
            String roleName = roleType.newInstance().getLogicalName();
            return SecurityUtils.getSubject().hasRole(roleName);
        } catch (InstantiationException | IllegalAccessException e) {
            // FIXME throw application exception
            throw new RuntimeException(e);
        }
    }

    public Long getUserId() {
        return (Long) SecurityUtils.getSubject().getPrincipal();
    }

    public SecuredUser getUser() {
        Long key = getUserId();
        return realmDao.getByPrimaryKeyReturnAll(key);
    }

    @Override
    public void logout() {
        SecurityUtils.getSubject().logout();
    }

    @Override
    public boolean isLoggedIn() {
        return SecurityUtils.getSubject().isAuthenticated();
    }
    
    @Override
    public boolean hasOneOfRoles(List<Class<? extends Role>> roleTypes){
        for(Class<? extends Role> roleType : roleTypes){
            if(hasRole(roleType)){
                return true;
            }
        }
        
        return false;
    }

    @Override
    public boolean authorizedToModifyVessel(Long mmsi) {
        logger.debug("authorizedToModifyVessel({})", mmsi);
        if(!hasRole(SailorRole.class)){
            logger.debug("authorizedToModifyVessel({}) - not Sailor", mmsi);
            return false;
        }

        SailorRole sailor = realmDao.getSailor((Long)SecurityUtils.getSubject().getPrincipal());
        logger.debug("authorizedToModifyVessel({}) - sailor={}", mmsi, sailor);

        if(sailor != null){
            logger.debug("authorizedToModifyVessel({}) - vessel={}", mmsi, sailor.getVessel());
        }

        return mmsi.equals(sailor.getVessel().getMmsi());
    }


    @Override
    public boolean authorizedToModifyRoute(String enavId) {
        if(!hasRole(SailorRole.class)){
            return false;
        }

        if(enavId == null || enavId.length() == 0){
            return false;
        }

        Long mmsi = scheduleDao.getMmsiByRouteEnavId(enavId);
        
        if(mmsi == null){
            return false;
        }
        
        return authorizedToModifyVessel(mmsi);
    }
    
    @Override
    public boolean authorizedToModifyVoyage(String enavId) {
        if(!hasRole(SailorRole.class)){
            return false;
        }
        
        if(enavId == null || enavId.length() == 0){
            return false;
        }

        Long mmsi = scheduleDao.getMmsiByVoyageEnavId(enavId);
        if(mmsi == null){
            return false;
        }
        
        return authorizedToModifyVessel(mmsi);
    }
    
}
