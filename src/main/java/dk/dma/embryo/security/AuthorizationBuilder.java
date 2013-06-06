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

import javax.inject.Inject;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import dk.dma.arcticweb.dao.RealmDao;

public class AuthorizationBuilder {

    @Inject
    private RealmDao realmDao;

    public AuthorizationBuilder createUser(String userName) {

        Subject s = SecurityUtils.getSecurityManager().createSubject(null);
        //
        // AuthorizationManager au;
        // au.

        return this;
    }

    // public AuthorizationBuilder createRole(String name){
    // role = new Role(new Text("en", name));
    // return this;
    // }

    public AuthorizationBuilder addFeatureGroup(String name) {
        return null;
    }

    public AuthorizationBuilder addFeatureToRole(String name) {
        return null;
    }

}
