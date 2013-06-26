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
package dk.dma.arcticweb.service;


import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import dk.dma.arcticweb.dao.RealmDao;
import dk.dma.embryo.config.Configuration;
import dk.dma.embryo.domain.Role;
import dk.dma.embryo.domain.SecuredUser;

public class JpaRealm extends AuthorizingRealm {

    public static final String REALM = "EmbryonicJpaRealm";

    public JpaRealm() {
        setName(REALM); // This name must match the name in the User class's getPrincipals() method
    }

    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) {
        RealmDao realmDao = Configuration.getBean(RealmDao.class);
        
        UsernamePasswordToken token = (UsernamePasswordToken) authcToken;
        SecuredUser user = realmDao.findByUsername(token.getUsername());
        if (user != null) {
            return new SimpleAuthenticationInfo(user.getId(), user.getPassword(), getName());
        } else {
            return null;
        }
    }

    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        RealmDao realmDao = Configuration.getBean(RealmDao.class);

        Long userId = (Long) principals.fromRealm(getName()).iterator().next();
        SecuredUser user = realmDao.getByPrimaryKeyReturnAll(userId);
        if (user != null) {
            SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
            for (Role role : user.getRoles()) {
                info.addRole(role.getLogicalName());
                info.addStringPermissions(role.getPermissionsAsStrings());
            }
            return info;
        } else {
            return null;
        }
    }
}
