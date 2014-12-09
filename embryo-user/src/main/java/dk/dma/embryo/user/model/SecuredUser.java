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
package dk.dma.embryo.user.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import dk.dma.embryo.common.persistence.BaseEntity;

@Entity
@NamedQueries({
    @NamedQuery(name = "SecuredUser:findByUserName", query = "SELECT u FROM SecuredUser u WHERE u.userName=:userName"),
    // LEFT JOIN FETCH u.roles [identification variable] is not supported by JPA but by Hibernate
    @NamedQuery(name = "SecuredUser:getByPrimaryKeyReturnAll", query = "SELECT u FROM SecuredUser u LEFT JOIN FETCH u.role WHERE u.id=:id"),
    @NamedQuery(name = "SecuredUser:list", query = "SELECT u FROM SecuredUser u LEFT JOIN FETCH u.role AS r LEFT JOIN FETCH r.vessel"),
    @NamedQuery(name="SecuredUser:findByEmail", query = "SELECT u FROM SecuredUser u WHERE u.email=:email"),
    @NamedQuery(name="SecuredUser:findByUuid", query = "SELECT u FROM SecuredUser u WHERE u.forgotUuid=:uuid")})
public class SecuredUser extends BaseEntity<Long> {

    private static final long serialVersionUID = -8480232439011093135L;

    // //////////////////////////////////////////////////////////////////////
    // Entity fields (also see super class)
    // //////////////////////////////////////////////////////////////////////

    private String hashedPassword;

    private byte[] salt;

    @Column(unique=true)
    private String userName;

    private String email;

    private String forgotUuid;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime created;

    @OneToOne(cascade=CascadeType.REMOVE)
    private Role role;

    @OneToMany(orphanRemoval = true ,cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    @JoinColumn(name="SecuredUser_id", nullable = false)
    private List<SelectionGroup> selectionGroups = new ArrayList<SelectionGroup>();

    public SecuredUser() {}

    public SecuredUser(String userName, String hashedPassword, byte[] salt) {
        setUserName(userName);
        setHashedPassword(hashedPassword);
        setSalt(salt);
        created = DateTime.now(DateTimeZone.UTC);
    }

    public SecuredUser(String userName, String hashedPassword, byte[] salt, String email) {
        this(userName, hashedPassword, salt);
        setEmail(email);
    }

    public void addSelectionGroup(SelectionGroup group) {

        if(this.selectionGroups == null) {
            this.selectionGroups = new ArrayList<SelectionGroup>();
        }

        this.selectionGroups.add(group);
    }

    // //////////////////////////////////////////////////////////////////////
    // Object methods
    // //////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
        return "SecuredUser [userName=" + userName + ", id=" + id + ", email=" + email + " hashedpassword=*]";
    }
    public boolean hasActiveSelectionGroups() {

        if(this.selectionGroups != null && !this.selectionGroups.isEmpty()) {
            for (SelectionGroup group : this.selectionGroups) {
                if(group.getActive()) {
                    return true;
                }
            }
        } 

        return false;
    }

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
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

    // public Set<Permission> getPermissions() {
    // return permissions;
    // }

    public String getHashedPassword() {
        return hashedPassword;
    }
    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public byte[] getSalt() {
        return salt;
    }
    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public DateTime getCreated() {
        return created;
    }

    public Role getRole() {
        return role;
    }
    public void setRole(Role role) {
        this.role = role;
        //roles.add(role);
        //role.users.add(this);
    }

    public String getForgotUuid() {
        return forgotUuid;
    }
    public void setForgotUuid(String forgotUuid) {
        this.forgotUuid = forgotUuid;
    }

    public List<SelectionGroup> getSelectionGroups() {
        return selectionGroups;
    }
    public void setSelectionGroups(List<SelectionGroup> selectionGroups) {
        this.selectionGroups = selectionGroups;
    }

}
