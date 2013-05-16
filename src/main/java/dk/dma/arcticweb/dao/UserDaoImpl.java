package dk.dma.arcticweb.dao;

import javax.ejb.Stateless;
import javax.persistence.Query;

import dk.dma.arcticweb.domain.User;

@Stateless
public class UserDaoImpl extends DaoImpl implements UserDao {
	
	@SuppressWarnings("unchecked")
	@Override
	public User getByUsername(String username) {
		Query query = em.createNamedQuery("User:getByUsername");
		query.setParameter("username", username);
		return (User) getSingleOrNull(query.getResultList());
	}
		
	@Override
	public boolean userExists(String username) {
		return (getByUsername(username) != null);
	}

}
