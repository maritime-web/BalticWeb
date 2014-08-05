/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.embryo.vessel.persistence;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.joda.time.DateTime;

import dk.dma.embryo.common.persistence.DaoImpl;
import dk.dma.embryo.vessel.model.Route;
import dk.dma.embryo.vessel.model.Vessel;
import dk.dma.embryo.vessel.model.Voyage;

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
    
    @Override
    public Long getMmsiByRouteEnavId(String enavId){
        TypedQuery<Long> query = em.createNamedQuery("Route:mmsi", Long.class);
        query.setParameter("enavId", enavId);
        List<Long> result = query.getResultList();
        return getSingleOrNull(result);
        
    }

    @Override
    public Long getMmsiByVoyageEnavId(String enavId){
        TypedQuery<Long> query = em.createNamedQuery("Voyage:mmsi", Long.class);
        query.setParameter("enavId", enavId);
        List<Long> result = query.getResultList();
        return getSingleOrNull(result);
        
    }

}
