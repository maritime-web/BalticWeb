package dk.dma.arcticweb.service;

import javax.ejb.Local;

import dk.dma.arcticweb.domain.User;

@Local
public interface UserService {
	
	/**
	 * Get user given username and password. Null if invalid
	 * username or password.
	 * @param username
	 * @param password
	 * @return
	 */
	User login(String username, String password);
	
	/**
	 * Create a new user
	 * @param user
	 * @return
	 */
	User createUser(User user);
	
	/**
	 * Get fresh copy of user
	 * @param user
	 * @return
	 */
	User get(User user);
	

}
