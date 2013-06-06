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
package dk.dma.arcticweb.domain;

import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;

import org.apache.commons.codec.digest.DigestUtils;

import dk.dma.embryo.domain.Role;

@Entity
@NamedQueries({ @NamedQuery(name = "User:getByUsername", query = "SELECT u FROM User u WHERE u.username=:username") })
public class User extends AbstractEntity {  

    private static final long serialVersionUID = 1L;

    private static final String PASSWORD_SALT = "fa26frADu8";

    private String username;
    private String passwordHash;
    private String email;
    private Date lastLogin;
    private Stakeholder stakeholder;
    
    public User() {
        super();
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    @Override
    public Integer getId() {
        return id;
    }

    @Column(unique = true, nullable = false, length = 32)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Column(nullable = false, length = 256)
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passowordHash) {
        this.passwordHash = passowordHash;
    }

    @Column(nullable = false, length = 128)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Column(nullable = true)
    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(nullable = true)
    public Stakeholder getStakeholder() {
        return stakeholder;
    }

    public void setStakeholder(Stakeholder stakeholder) {
        this.stakeholder = stakeholder;
    }

    @Transient
    public void setPassword(String password) {
        setPasswordHash(hashPassword(password));
    }

    @Transient
    public boolean passwordMatch(String password) {
        if (password == null){
            return false;
        }
        return hashPassword(password).equals(getPasswordHash());
    }

    @Transient
    public static String hashPassword(String password) {
        return DigestUtils.sha256Hex(DigestUtils.sha256Hex(password)
                + PASSWORD_SALT);
    }

    public static void main(String[] args) {
        String password = "qwerty";
        if (args.length > 0) {
            password = args[0];
        }
        String hash = hashPassword(password);
        System.out.println("password     : " + password);
        System.out.println("password hash: " + hash);
    }

}
