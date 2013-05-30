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

import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import dk.dma.arcticweb.dao.UserDao;
import dk.dma.arcticweb.domain.User;

@Stateless
public class UserServiceImpl implements UserService {
	
	@EJB
	private UserDao userDao;
	
	@Override
	public User login(String username, String password) {
		User user = userDao.getByUsername(username);
		if (user == null) {
			return null;
		}
		if (!user.passwordMatch(password)) {
			return null;
		}
		user.setLastLogin(new Date());
		return (User)userDao.saveEntity(user);
	}
	
	@Override
	public User createUser(User user) {
		return (User)userDao.saveEntity(user);
	}
	
	@Override
	public User get(User user) {
		return (User)userDao.getByPrimaryKey(User.class, user.getId());
	}
	
}
