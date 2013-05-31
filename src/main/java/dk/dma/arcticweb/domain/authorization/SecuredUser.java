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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import dk.dma.arcticweb.domain.BaseEntity;

@Entity
@NamedQueries({
        @NamedQuery(name = "SecuredUser:findByUserName", query = "SELECT u FROM SecuredUser u WHERE u.userName=:userName"),
        @NamedQuery(name = "SecuredUser:getByPrimaryKeyReturnAll", query = "SELECT u FROM SecuredUser u LEFT JOIN FETCH u.roles WHERE u.id=:id") })
public class SecuredUser extends BaseEntity<Long> {

    private static final long serialVersionUID = -8480232439011093135L;

    public SecuredUser() {
    }

    public SecuredUser(String userName, String password) {
        setUserName(userName);
        setPassword(password);
    }

    // //////////////////////////////////////////////////////////////////////
    // Entity fields (also see super class)
    // //////////////////////////////////////////////////////////////////////

    private String password;

    private String userName;

    @ManyToMany(mappedBy = "users")
    private Set<Permission> permissions;

    @ManyToMany
    private List<Role> roles = new ArrayList<>(5);

    // //////////////////////////////////////////////////////////////////////
    // business logic
    // //////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
        return "SecuredUser [password=" + password + ", userName=" + userName + ", id=" + id + "]";
    }
    
    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public List<Role> getRoles() {
        return Collections.unmodifiableList(roles);
    }

    public void addRole(Role role) {
        roles.add(role);
    }
}
