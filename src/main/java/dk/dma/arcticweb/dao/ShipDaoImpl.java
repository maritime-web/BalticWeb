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

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import dk.dma.embryo.domain.Route;
import dk.dma.embryo.domain.Sailor;
import dk.dma.embryo.domain.Ship2;
import dk.dma.embryo.domain.Voyage;
import dk.dma.embryo.domain.VoyagePlan;

@Stateless
public class ShipDaoImpl extends DaoImpl implements ShipDao {

    public ShipDaoImpl() {
        super();
    }

    public ShipDaoImpl(EntityManager entityManager) {
        super(entityManager);
    }


    @Override
    public Ship2 getShip(Sailor sailor) {
        Long shipId = sailor.getShip().getId();
        return (Ship2)getByPrimaryKey(Ship2.class, shipId);
    }


    @Override
    public VoyagePlan getVoyagePlan(Long mmsi) {
        TypedQuery<VoyagePlan> query = em.createNamedQuery("VoyagePlan:getByMmsi", VoyagePlan.class);
        query.setParameter("mmsi", mmsi);
        
        List<VoyagePlan> result = query.getResultList();
        
        return getSingleOrNull(result);
    }



    @Override
    public Route getActiveRoute(Long mmsi) {
        TypedQuery<Ship2> query = em.createNamedQuery("Ship:getByMmsi", Ship2.class);
        query.setParameter("mmsi", mmsi);
        
        List<Ship2> result = query.getResultList();
        
        Ship2 ship = getSingleOrNull(result); 
        
        return ship == null ? null : ship.getActiveRoute();
    }

    @Override
    public Long getRouteId(String enavId) {
        TypedQuery<Long> query = em.createNamedQuery("Route:getId", Long.class);
        query.setParameter("enavId", enavId);
        
        List<Long> result = query.getResultList();
        
        return getSingleOrNull(result);
    }

    @Override
    public Route getRouteByEnavId(String enavId) {
        TypedQuery<Route> query = em.createNamedQuery("Route:getByEnavId", Route.class);
        query.setParameter("enavId", enavId);
        
        List<Route> result = query.getResultList();
        
        return getSingleOrNull(result);
    }

    @Override
    public Voyage getVoyageByEnavId(String enavId) {
        TypedQuery<Voyage> query = em.createNamedQuery("Voyage:getByEnavId", Voyage.class);
        query.setParameter("enavId", enavId);
        
        List<Voyage> result = query.getResultList();
        
        return getSingleOrNull(result);
    }
}
