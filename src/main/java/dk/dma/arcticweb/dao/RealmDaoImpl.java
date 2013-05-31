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
import javax.persistence.TypedQuery;

import dk.dma.arcticweb.domain.authorization.Permission;
import dk.dma.arcticweb.domain.authorization.Role;
import dk.dma.arcticweb.domain.authorization.SecuredUser;

@Stateless
public class RealmDaoImpl extends DaoImpl implements RealmDao {

    @Inject
    public RealmDaoImpl(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public SecuredUser findByUsername(String username) {
        TypedQuery<SecuredUser> query = em.createNamedQuery("SecuredUser:findByUserName", SecuredUser.class);
        query.setParameter("userName", username);

        return (SecuredUser) getSingleOrNull(query.getResultList());
    }

    @Override
    public SecuredUser getByPrimaryKeyReturnAll(Long key) {
        TypedQuery<SecuredUser> query = em.createNamedQuery("SecuredUser:getByPrimaryKeyReturnAll", SecuredUser.class);
        query.setParameter("id", key);

        return (SecuredUser) getSingleOrNull(query.getResultList());
    }

    public Role saveEntity(Role entity) {
        return (Role) saveEntity(entity);
    }

    public Permission saveEntity(Permission entity) {
        return (Permission) saveEntity(entity);
    }

}
