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
package dk.dma.embryo.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import dk.dma.arcticweb.domain.BaseEntity;

@Entity
@NamedQueries({
        @NamedQuery(name = "SecuredUser:findByUserName", query = "SELECT u FROM SecuredUser u WHERE u.userName=:userName"),
        // LEFT JOIN FETCH u.roles [identification variable] is not supported by JPA but by Hibernate
       @NamedQuery(name = "SecuredUser:getByPrimaryKeyReturnAll", query = "SELECT u FROM SecuredUser u LEFT JOIN FETCH u.roles AS r LEFT JOIN FETCH r.permissions WHERE u.id=:id") })
public class SecuredUser extends BaseEntity<Long> {

    private static final long serialVersionUID = -8480232439011093135L;

    public SecuredUser() {
    }

    public SecuredUser(String userName, String password) {
        setUserName(userName);
        setPassword(password);
    }

    public SecuredUser(String userName, String password, String email) {
        this(userName, password);
        setEmail(email);
    }

    // //////////////////////////////////////////////////////////////////////
    // Entity fields (also see super class)
    // //////////////////////////////////////////////////////////////////////

    private String password;

    private String userName;

    private String email;

    //@ManyToMany(mappedBy = "users")
    //private Set<Permission> permissions;

    @ManyToMany
    private Set<Role> roles = new HashSet<>();
    
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
    
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

//    public Set<Permission> getPermissions() {
//        return permissions;
//    }

    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public void addRole(Role role) {
        roles.add(role);
        role.users.add(this);
    }
    
    public Role getRole(String name){
        for(Role role : roles){
            if(role.getLogicalName().equals(name)){
                return role;
            }
        }
        
        return null;
    }

    // TODO For now we only expect one of each role type
    // with time this will not hold and the implementation must be changed. 
    public <R extends Role> R getRole(Class<R> type){
        for(Role role : roles){
            if(role.getClass() == type){
                return (R)role;
            }
        }
        
        return null;
    }
}
