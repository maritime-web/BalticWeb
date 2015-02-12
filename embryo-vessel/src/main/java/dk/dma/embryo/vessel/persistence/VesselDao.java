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
import java.util.Map;

import javax.ejb.Local;

import dk.dma.embryo.common.persistence.Dao;
import dk.dma.embryo.vessel.model.Route;
import dk.dma.embryo.vessel.model.Vessel;
import dk.dma.embryo.vessel.model.Voyage;

/**
 * The Interface VesselDao.
 */
@Local
public interface VesselDao extends Dao {

    /**
     * Gets the vessel by callsign.
     *
     * @param vessel callsign
     * @return the vessel by callsign
     */
    Vessel getVesselByCallsign(String callsign);

    /**
     * Gets the vessel.
     *
     * @param vessel mmsi
     * @return the vessel
     */
    Vessel getVessel(Long mmsi);
    
    /**
     * Gets the vessels by a list of MMSI numbers.
     *
     * @param List of MMSI numbers
     * @return all matching vessels
     */
    Map<Long, Vessel> getVessels(List<Long> mmsiNumbers);
    
    /**
     * Gets the active route by MMSI number.
     *
     * @param vessel MMSI
     * @return the active route
     */
    Route getActiveRoute(Long mmsi);

    /**
     * Gets the route id by enav id.
     *
     * @param enavId
     * @return the route id
     */
    Long getRouteId(String enavId);

    /**
     * Gets the route by enav id.
     *
     * @param enav id
     * @return the route by enav id
     */
    Route getRouteByEnavId(String enavId);

    /**
     * Gets the voyage by enav id.
     *
     * @param enav id
     * @return the voyage by enav id
     */
    Voyage getVoyageByEnavId(String enavId);
}
