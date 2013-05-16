package dk.dma.arcticweb.dao;

import javax.ejb.Local;

import dk.dma.arcticweb.domain.User;

@Local
public interface UserDao extends Dao {
	
	User getByUsername(String username);
	
	boolean userExists(String username);

}
