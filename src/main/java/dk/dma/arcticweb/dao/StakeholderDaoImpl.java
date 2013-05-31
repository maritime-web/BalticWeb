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

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import dk.dma.arcticweb.domain.Authority;
import dk.dma.arcticweb.domain.IEntity;
import dk.dma.arcticweb.domain.Ship;
import dk.dma.arcticweb.domain.ShipOwner;
import dk.dma.arcticweb.domain.ShoreStakeholder;
import dk.dma.arcticweb.domain.Stakeholder;

@Stateless
public class StakeholderDaoImpl extends DaoImpl implements StakeholderDao {

    @Inject
    public StakeholderDaoImpl(EntityManager entityManager) {
        super(entityManager);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Stakeholder> getAll() {
        Query query = em.createQuery("SELECT s FROM Stakeholder s");
        return query.getResultList();
    }

    @Override
    public Stakeholder get(int id) {
        // Special handling to get instance of the correct type
        List<Class<? extends IEntity>> stakeholderClasses = new ArrayList<>();
        stakeholderClasses.add(Ship.class);
        stakeholderClasses.add(Authority.class);
        stakeholderClasses.add(ShipOwner.class);
        stakeholderClasses.add(ShoreStakeholder.class);
        for (Class<? extends IEntity> stakeholderClass : stakeholderClasses) {
            IEntity stakeholder = getByPrimaryKey(stakeholderClass, id);
            if (stakeholder != null) {
                return (Stakeholder) stakeholder;
            }
        }
        return null;
    }

}
