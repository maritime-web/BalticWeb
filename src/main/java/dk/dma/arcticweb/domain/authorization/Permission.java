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
package dk.dma.arcticweb.domain.authorization;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;

@Entity
public class Permission extends AbstractAuthorizationEntity<Integer> {

    private static final long serialVersionUID = 6625306470923937976L;

    // //////////////////////////////////////////////////////////////////////
    // Entity fields (also see super class)
    // //////////////////////////////////////////////////////////////////////
    @ManyToMany
    private Set<SecuredUser> users;

    @ManyToMany
    private Set<Role> roles;

    // //////////////////////////////////////////////////////////////////////
    // business logic
    // //////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public Set<SecuredUser> getUsers() {
        return users;
    }

    public void setUsers(Set<SecuredUser> users) {
        this.users = users;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}
