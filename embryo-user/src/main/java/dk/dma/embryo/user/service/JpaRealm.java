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
package dk.dma.embryo.user.service;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;

import dk.dma.embryo.common.configuration.Configuration;
import dk.dma.embryo.user.model.SecuredUser;
import dk.dma.embryo.user.persistence.RealmDao;

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
            return new SimpleAuthenticationInfo(user.getId(), user.getHashedPassword(), ByteSource.Util.bytes(user
                    .getSalt()), getName());
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
            info.addRole(user.getRole().getLogicalName());
            return info;
        } else {
            return null;
        }
    }
}
