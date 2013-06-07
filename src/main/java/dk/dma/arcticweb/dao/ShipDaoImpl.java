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

import dk.dma.embryo.domain.Sailor;
import dk.dma.embryo.domain.Ship2;

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
