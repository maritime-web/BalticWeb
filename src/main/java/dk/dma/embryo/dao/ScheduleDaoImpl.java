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

import org.joda.time.DateTime;

import dk.dma.embryo.domain.Route;
import dk.dma.embryo.domain.Vessel;
import dk.dma.embryo.domain.Voyage;

@Stateless
public class ScheduleDaoImpl extends DaoImpl implements ScheduleDao {

    public ScheduleDaoImpl() {
        super();
    }

    public ScheduleDaoImpl(EntityManager entityManager) {
        super(entityManager);
    }


    @Override
    public List<Voyage> getSchedule(Long mmsi) {
        TypedQuery<DateTime> datequery = em.createQuery("select MAX(v.departure) from Voyage v where v.vessel.mmsi = :mmsi AND date(v.departure) < CURRENT_DATE()", DateTime.class);
        datequery.setParameter("mmsi", mmsi);
        DateTime date = datequery.getSingleResult();

        TypedQuery<DateTime> activeDateQuery = em.createQuery("select ves.activeVoyage.departure from Vessel ves where ves.mmsi = :mmsi", DateTime.class);
        activeDateQuery.setParameter("mmsi", mmsi);
        DateTime activeDate = getSingleOrNull(activeDateQuery.getResultList());

        if(date != null && activeDate != null && date.isAfter(activeDate)){
            date = activeDate;
        }else if (date == null){
            date = activeDate;
        }
        
        TypedQuery<Voyage> query = em.createNamedQuery("Voyage:getByMmsi", Voyage.class);
        query.setParameter("mmsi", mmsi);
        query.setParameter("date", date != null ? date.toDate() : null);

        List<Voyage> result = query.getResultList();

        return result;
    }

    @Override
    public Route getActiveRoute(Long mmsi) {
        TypedQuery<Vessel> query = em.createNamedQuery("Vessel:getByMmsi", Vessel.class);
        query.setParameter("mmsi", mmsi);

        List<Vessel> result = query.getResultList();

        Vessel vessel = getSingleOrNull(result);

        if (vessel == null || vessel.getActiveVoyage() == null) {
            return null;
        }

        return vessel.getActiveVoyage().getRoute();
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

    @Override
    public List<Voyage> getByEnavIds(List<String> enavIds) {
        TypedQuery<Voyage> query = em.createNamedQuery("Voyage:getByEnavIds", Voyage.class);
        query.setParameter("enavIds", enavIds);
        List<Voyage> result = query.getResultList();
        return result;
    }

}
