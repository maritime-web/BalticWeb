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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class Role extends AbstractAuthorizationEntity<Integer> {

    private static final long serialVersionUID = -8480232439011093135L;

    public Role() {
    }

    public Role(String logicalName) {
        super(logicalName);
    }

    public Role(String logicalName, Text name) {
        super(logicalName, name);
    }

    // //////////////////////////////////////////////////////////////////////
    // Entity fields (also see super class)
    // //////////////////////////////////////////////////////////////////////
    @ManyToMany
    private Set<Permission> permissions = new HashSet<>();

    @ManyToMany(mappedBy="roles")
    Set<SecuredUser> users = new HashSet<>();

    // //////////////////////////////////////////////////////////////////////
    // business logic
    // //////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////
    public Collection<String> getPermissionsAsStrings() {
        Collection<String> permissions = new HashSet<>();
        for (Permission permission : getPermissions()) {
            permissions.add(permission.getLogicalName());
        }
        return permissions;
    }

    @Override
    public String toString() {
        return "Role [" + super.toString() + "]";
    }
    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }
    
    public void add(Permission permission){
        // Maintain referential integrity
        permissions.add(permission);
        permission.roles.add(this);
    }

    public void remove(Permission permission){
        // Maintain referential integrity
        permissions.remove(permission);
        permission.roles.remove(this);
    }    

}
