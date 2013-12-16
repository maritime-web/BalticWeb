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

import java.io.Serializable;
import java.util.List;

import dk.dma.embryo.domain.Role;
import dk.dma.embryo.domain.SecuredUser;

/**
 * Subject class wrapping all access to shiro security and also decorating with extra syntactic sugar.
 * 
 * 
 * @author Jesper Tejlgaard
 * 
 */
public interface Subject extends Serializable {


    SecuredUser login(String userName, String password, Boolean rememberMe);
    
    SecuredUser login(String userName, String password);
    
    /**
     * TODO remove me.
     * 
     * Expected used while transitioning from role base security to feature base security
     * 
     * @param permission
     * @return
     */
    <R extends Role> boolean hasRole(Class<R> roleType) ;

    Long getUserId() ;

    SecuredUser getUser() ;

    boolean hasOneOfRoles(List<Class<? extends Role>> roleTypes) ;
    
    void logout();

    boolean isLoggedIn();
}
