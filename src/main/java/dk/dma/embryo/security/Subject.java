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
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;

import dk.dma.arcticweb.dao.RealmDao;
import dk.dma.embryo.config.IllegalConfigurationException;
import dk.dma.embryo.domain.Role;
import dk.dma.embryo.domain.SecuredUser;
import dk.dma.embryo.security.authorization.Permission;

/**
 * Subject class wrapping all access to shiro security and also decorating with extra syntactic sugar.
 * 
 * 
 * @author jesper
 * 
 */
@SessionScoped
public class Subject implements Serializable {

    @Inject
    RealmDao realmDao;

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

    public boolean isPermitted(Permission permission) {
        boolean result = SecurityUtils.getSubject().isPermitted(permission.value());
        System.out.println("isPermitted(" + permission + ") : " + result);
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

    public boolean isAtLeastOnePermitted(List<Permission> permissions) {
        System.out.println("isAtLeastOnePermitted(" + permissions + ")");
        if (!permissions.isEmpty()) {

            for (Permission permission : permissions) {
                System.out.print(permission + " ");

                if (isPermitted(permission)) {
                    System.out.println("isAtLeastOnePermitted(...) : true");
                    return true;
                }
            }
            System.out.println("isAtLeastOnePermitted(...) : false");
            return false;
        }

        return true;
    }

    public boolean isPermitted(Object annotated) {

        System.out.println("isPermitted(" + annotated + ")");

        List<Permission> permissions = getPermissions(annotated);
        return isAtLeastOnePermitted(permissions);
    }

    private List<Permission> getPermissions(Object object) {
        Annotation[] annotations = object.getClass().getAnnotations();

        if (annotations == null) {
            return Collections.emptyList();
        }
        List<Permission> permissions = new ArrayList<>(annotations.length);
        for (Annotation annotation : annotations) {
            Permission permission = annotation.annotationType().getAnnotation(Permission.class);

            if (permission != null) {
                if (permission.value() == null) {
                    // TODO replaceable with checkstyle rule
                    throw new IllegalConfigurationException("Invalid configuration in class "
                            + object.getClass().getSimpleName() + ". " + Permission.class.getSimpleName()
                            + " must have a value.");
                }
                permissions.add(permission);
            }

        }
        return permissions;
    }

    public void logout() {
        SecurityUtils.getSubject().logout();
    }

    public boolean isLoggedIn() {
        return SecurityUtils.getSubject().isAuthenticated();
    }

}
