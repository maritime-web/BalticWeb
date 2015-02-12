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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import dk.dma.embryo.common.persistence.DaoImpl;
import dk.dma.embryo.vessel.model.Route;
import dk.dma.embryo.vessel.model.Vessel;
import dk.dma.embryo.vessel.model.Voyage;

/**
 * The Class VesselDaoImpl.
 */
@Stateless
public class VesselDaoImpl extends DaoImpl implements VesselDao {

    /**
     * Instantiates a new vessel dao impl.
     */
    public VesselDaoImpl() {
        super();
    }

    /**
     * Instantiates a new vessel dao impl.
     *
     * @param entity manager
     */
    public VesselDaoImpl(EntityManager entityManager) {
        super(entityManager);
    }

    /* (non-Javadoc)
     * @see dk.dma.embryo.vessel.persistence.VesselDao#getVessel(java.lang.Long)
     */
    @Override
    public Vessel getVessel(Long mmsi) {
        TypedQuery<Vessel> query = em.createNamedQuery("Vessel:getByMmsi", Vessel.class);
        query.setParameter("mmsi", mmsi);

        List<Vessel> result = query.getResultList();

        return getSingleOrNull(result);
    }

    /* (non-Javadoc)
     * @see dk.dma.embryo.vessel.persistence.VesselDao#getVessels(java.util.List)
     */
    @Override
    public Map<Long, Vessel> getVessels(List<Long> mmsiNumbers) {
        TypedQuery<Vessel> query = em.createNamedQuery("Vessel:getMmsiList", Vessel.class);
        query.setParameter("mmsiNumbers", mmsiNumbers);

        List<Vessel> result = query.getResultList();
        Map<Long, Vessel> mapResult = new HashMap<>((result.size() +1) * (4/3));
        
        for(Vessel vessel : result){
            mapResult.put(vessel.getMmsi(), vessel);
        }
        return mapResult;
    }

    /* (non-Javadoc)
     * @see dk.dma.embryo.vessel.persistence.VesselDao#getVesselByCallsign(java.lang.String)
     */
    @Override
    public Vessel getVesselByCallsign(String callsign) {
        TypedQuery<Vessel> query = em.createNamedQuery("Vessel:getByCallsign", Vessel.class);
        query.setParameter("callsign", callsign);

        List<Vessel> result = query.getResultList();

        return getSingleOrNull(result);
    }

    /* (non-Javadoc)
     * @see dk.dma.embryo.vessel.persistence.VesselDao#getActiveRoute(java.lang.Long)
     */
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

    /* (non-Javadoc)
     * @see dk.dma.embryo.vessel.persistence.VesselDao#getRouteId(java.lang.String)
     */
    @Override
    public Long getRouteId(String enavId) {
        TypedQuery<Long> query = em.createNamedQuery("Route:getId", Long.class);
        query.setParameter("enavId", enavId);

        List<Long> result = query.getResultList();

        return getSingleOrNull(result);
    }

    /* (non-Javadoc)
     * @see dk.dma.embryo.vessel.persistence.VesselDao#getRouteByEnavId(java.lang.String)
     */
    @Override
    public Route getRouteByEnavId(String enavId) {
        TypedQuery<Route> query = em.createNamedQuery("Route:getByEnavId", Route.class);
        query.setParameter("enavId", enavId);

        List<Route> result = query.getResultList();

        return getSingleOrNull(result);
    }

    /* (non-Javadoc)
     * @see dk.dma.embryo.vessel.persistence.VesselDao#getVoyageByEnavId(java.lang.String)
     */
    @Override
    public Voyage getVoyageByEnavId(String enavId) {
        TypedQuery<Voyage> query = em.createNamedQuery("Voyage:getByEnavId", Voyage.class);
        query.setParameter("enavId", enavId);

        List<Voyage> result = query.getResultList();

        return getSingleOrNull(result);
    }

}
