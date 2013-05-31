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
package dk.dma.arcticweb.site.config;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.Realm;

@ApplicationScoped
public class Configuration implements Serializable {

    private static final long serialVersionUID = 5538000455989826397L;

    @Produces
    public void security() {

        Realm realm = new Realm() {

            @Override
            public boolean supports(AuthenticationToken token) {
                return false;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public AuthenticationInfo getAuthenticationInfo(AuthenticationToken token) {
                return null;
            }
        };

        DefaultSecurityManager securityManager = new DefaultSecurityManager();
    }

}
