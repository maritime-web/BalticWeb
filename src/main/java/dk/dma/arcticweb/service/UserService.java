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
package dk.dma.arcticweb.service;

import javax.ejb.Local;

import dk.dma.embryo.domain.User;

@Local
public interface UserService {

    /**
     * Get user given username and password. Null if invalid username or password.
     * 
     * @param username
     * @param password
     * @return
     */
    User login(String username, String password);

    /**
     * Create a new user
     * 
     * @param user
     * @return
     */
    User createUser(User user);

    /**
     * Get fresh copy of user
     * 
     * @param user
     * @return
     */
    User get(User user);

}
