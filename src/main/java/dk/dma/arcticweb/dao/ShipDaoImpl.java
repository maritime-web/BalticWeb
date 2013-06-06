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
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import dk.dma.arcticweb.domain.Authority;
import dk.dma.arcticweb.domain.IEntity;
import dk.dma.arcticweb.domain.Ship;
import dk.dma.arcticweb.domain.ShipOwner;
import dk.dma.arcticweb.domain.ShoreStakeholder;
import dk.dma.arcticweb.domain.Stakeholder;
import dk.dma.arcticweb.domain.authorization.Sailor;
import dk.dma.arcticweb.domain.authorization.Ship2;

@Stateless
public class ShipDaoImpl extends DaoImpl implements ShipDao {

    @Inject
    public ShipDaoImpl(EntityManager entityManager) {
        super(entityManager);
    }


    @Override
    public Ship2 getShip(Sailor sailor) {
        Long shipId = sailor.getShip().getId();
        
        return (Ship2)getByPrimaryKey(Ship2.class, shipId);
    }

}
