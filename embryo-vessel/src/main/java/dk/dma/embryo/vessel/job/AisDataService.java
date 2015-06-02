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
package dk.dma.embryo.vessel.job;

import java.util.List;
import java.util.Map;

import dk.dma.embryo.vessel.json.client.Vessel;

public interface AisDataService {
   
    List<Vessel> getAisVessels(boolean withExactEarth, boolean userHasAnyActiveSelectionGroups);
    Map<Long, dk.dma.embryo.vessel.model.Vessel> updateArcticWebVesselInDatabase(List<Vessel> aisVessels, List<dk.dma.embryo.vessel.model.Vessel> articWebVesselsAsList);
    
    boolean isWithinAisCircle(double longitude, double latitude);
    boolean isAllowed(double latitude);
    Vessel getAisVesselByMmsi(Long mmsi);

        
    List<Vessel> getVesselsAllowed();
    void setVesselsAllowed(List<Vessel> vesselsAllowed);
    
    List<Vessel> getVesselsInAisCircle();
    void setVesselsInAisCircle(List<Vessel> vesselsInArcticCircle);

    List<Vessel> getVesselsOnMap();
    void setVesselsOnMap(List<Vessel> vesselsInArcticCircle);
    
}
