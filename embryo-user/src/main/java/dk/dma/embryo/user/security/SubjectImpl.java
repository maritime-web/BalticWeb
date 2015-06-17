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
package dk.dma.embryo.user.security;

import dk.dma.embryo.user.model.Role;
import dk.dma.embryo.user.model.SailorRole;
import dk.dma.embryo.user.model.SecuredUser;
import dk.dma.embryo.user.persistence.RealmDao;
import dk.dma.embryo.vessel.persistence.ScheduleDao;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.slf4j.Logger;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import java.util.List;

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
    
    @Override
    public SecuredUser findUserWithUuid(String uuid) {
        SecuredUser user = realmDao.findByUuid(uuid);
        return user;
    }

    @Override
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
    public SecuredUser getUserForEmail(String email) {
        return realmDao.findByEmail(email);
    }
    
}
