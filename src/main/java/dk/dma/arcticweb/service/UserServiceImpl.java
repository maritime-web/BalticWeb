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
