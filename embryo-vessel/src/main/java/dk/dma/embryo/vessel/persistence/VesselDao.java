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

@Local
public interface VesselDao extends Dao {

    /**
     * 
     * @param sailor
     * @return
     */

    Vessel getVesselByCallsign(String callsign);

    Vessel getVessel(Long mmsi);
    
    Map<Long, Vessel> getVessels(List<Long> mmsiNumbers);
    
    Route getActiveRoute(Long mmsi);

    Long getRouteId(String enavId);

    Route getRouteByEnavId(String enavId);

    Voyage getVoyageByEnavId(String enavId);
}
