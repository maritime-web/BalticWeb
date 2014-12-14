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

import dk.dma.embryo.vessel.job.MaxSpeedJob.MaxSpeedRecording;
import dk.dma.embryo.vessel.json.client.AisViewServiceAllAisData;

public interface AisDataService {
   
    boolean isWithinAisCircle(double longitude, double latitude);
    boolean isAllowed(double latitude);
    AisViewServiceAllAisData.Vessel getAisVesselByMmsi(Long mmsi);
    
    List<AisViewServiceAllAisData.Vessel> getVesselsAllowed();
    void setVesselsAllowed(List<AisViewServiceAllAisData.Vessel> vesselsAllowed);
    
    List<AisViewServiceAllAisData.Vessel> getVesselsInAisCircle();
    void setVesselsInAisCircle(List<AisViewServiceAllAisData.Vessel> vesselsInArcticCircle);

    List<AisViewServiceAllAisData.Vessel> getVesselsOnMap();
    void setVesselsOnMap(List<AisViewServiceAllAisData.Vessel> vesselsInArcticCircle);

    Map<Long, MaxSpeedRecording> getMaxSpeeds();
    void setMaxSpeeds(Map<Long, MaxSpeedRecording> maxSpeeds);
}
