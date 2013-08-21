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

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.wicket.MarkupContainer;
import org.slf4j.Logger;

import dk.dma.arcticweb.dao.RealmDao;
import dk.dma.embryo.domain.Role;
import dk.dma.embryo.domain.SecuredUser;
import dk.dma.embryo.security.authorization.DependsOnChildPermissionContainers;
import dk.dma.embryo.security.authorization.IsChildPermissionContainersAuthorized;
import dk.dma.embryo.security.authorization.Permission;

/**
 * Subject class wrapping all access to shiro security and also decorating with extra syntactic sugar.
 * 
 * 
 * @author Jesper Tejlgaard
 * 
 */
@SessionScoped
public class Subject implements Serializable {

    private static final long serialVersionUID = -7771436245663646148L;

    @Inject
    private transient RealmDao realmDao;

    @Inject
    private transient Logger logger;

    @Inject
    private PermissionExtractor permissionExtractor;

    public SecuredUser login(String userName, String password, Boolean rememberMe) {
        // collect user principals and credentials in a gui specific manner
        // such as username/password html form, X509 certificate, OpenID, etc.
        // We'll use the username/password example here since it is the most common.
        // (do you know what movie this is from? ;)
        UsernamePasswordToken token = new UsernamePasswordToken(userName, password, rememberMe);
        // this is all you have to do to support 'remember me' (no config - built in!):
        // token.setRememberMe(true);
        SecurityUtils.getSubject().login(token);

        logger.info("AIS access: " + SecurityUtils.getSubject().isPermitted("ais"));
        logger.info("YourShip access: " + SecurityUtils.getSubject().isPermitted("yourShip"));

        return realmDao.findByUsername(userName);
    }

    public SecuredUser login(String userName, String password) {
        return login(userName, password, Boolean.FALSE);
    }

    public boolean isPermitted(Permission permission) {
        boolean result = SecurityUtils.getSubject().isPermitted(permission.value());
        logger.trace("isPermitted({}) : {}" + permission, result);
        return result;
    }

    /**
     * TODO remove me.
     * 
     * Expected used while transitioning from role base security to feature base security
     * 
     * @param permission
     * @return
     */
    public boolean isPermitted(String permission) {
        return SecurityUtils.getSubject().isPermitted(permission);
    }

    /**
     * TODO remove me.
     * 
     * Expected used while transitioning from role base security to feature base security
     * 
     * @param permission
     * @return
     */
    public <R extends Role> boolean hasRole(Class<R> roleType) {
        try {
            return SecurityUtils.getSubject().hasRole(roleType.newInstance().getLogicalName());
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

    public <R extends Role> R getRole(Class<R> roleType) {
        return getUser().getRole(roleType);
    }

    public boolean isAtLeastOnePermitted(Permission[] permissions) {
        if (permissions.length > 0) {
            for (Permission permission : permissions) {
                if (isPermitted(permission)) {
                    return true;
                }
            }
            return false;
        }

        return true;
    }

    public boolean isPermitted(Object secured) {
        if (secured instanceof PermissionContainer) {
            PermissionContainer permissionContainer = (PermissionContainer) secured;
            if (permissionContainer.hasPermissions()) {
                return isAtLeastOnePermitted(permissionContainer.getPermissions());
            }
            // fall through to security by original object
            // should this instead just return true ?
        }

        if (secured.getClass().getAnnotation(DependsOnChildPermissionContainers.class) != null) {
            logger.debug("Checking child permission container for object {}", secured);
            MarkupContainer component = (MarkupContainer) secured;
            IsChildPermissionContainersAuthorized visitor = new IsChildPermissionContainersAuthorized(this, permissionExtractor);
            component.visitChildren(PermissionContainer.class, visitor);
            return visitor.getResult();
        }

        // // DEAD CODE?
        // if(secured instanceof PermissionDelegator<?>){
        // PermissionDelegator<?> permissionDelegate = (PermissionDelegator<?>)secured;
        // if(permissionDelegate.hasPermissionDelegate()){
        // Permission[] permissions = permissionExtractor.getPermissions(permissionDelegate.getPermissionDelegate());
        // return isAtLeastOnePermitted(permissions);
        // }
        // // fall through to security by original object
        // // should this instead just return true ?
        // }

        Permission[] permissions = permissionExtractor.getPermissions(secured);
        return isAtLeastOnePermitted(permissions);
    }

    public void logout() {
        SecurityUtils.getSubject().logout();
    }

    public boolean isLoggedIn() {
        return SecurityUtils.getSubject().isAuthenticated();
    }

}
