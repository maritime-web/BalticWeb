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
package dk.dma.embryo.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import dk.dma.embryo.common.persistence.DaoImpl;
import dk.dma.embryo.domain.SailorRole;
import dk.dma.embryo.domain.SecuredUser;

@Stateless
public class RealmDaoImpl extends DaoImpl implements RealmDao {

    public RealmDaoImpl() {
        super();
    }

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

    @Override
    public SailorRole getSailor(Long userId) {
        TypedQuery<SailorRole> query = em.createNamedQuery("Sailor:withVessel", SailorRole.class);
        query.setParameter("id", userId);
        return query.getSingleResult();
    }

    public List<SecuredUser> list(){
        TypedQuery<SecuredUser> query = em.createNamedQuery("SecuredUser:list", SecuredUser.class);
        return query.getResultList();
    }

}
