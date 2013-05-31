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
package dk.dma.arcticweb.dao;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import dk.dma.arcticweb.domain.User;

@Stateless
public class UserDaoImpl extends DaoImpl implements UserDao {

    @Inject
    public UserDaoImpl(EntityManager entityManager) {
        super(entityManager);
    }

    @SuppressWarnings("unchecked")
    @Override
    public User getByUsername(String username) {
        Query query = em.createNamedQuery("User:getByUsername");
        query.setParameter("username", username);
        return (User) getSingleOrNull(query.getResultList());
    }

    @Override
    public boolean userExists(String username) {
        return getByUsername(username) != null;
    }

}
