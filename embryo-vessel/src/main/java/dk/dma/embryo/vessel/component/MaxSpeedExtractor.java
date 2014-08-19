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
package dk.dma.embryo.vessel.component;

import java.util.List;
import java.util.Map;

import dk.dma.embryo.vessel.job.MaxSpeedJob.MaxSpeedRecording;

/**
 * @author Jesper Tejlgaard
 */
public class MaxSpeedExtractor {

    public MaxSpeedRecording extractMaxSpeed(Map result) {
        double maxValue = 0;
        
        Map<String, List<Map<String, Object>>> pastTrack = (Map<String, List<Map<String, Object>>>)result.get("pastTrack");
        if(pastTrack == null){
            return new MaxSpeedRecording(maxValue);
        }

        List<Map<String, Object>> points = pastTrack.get("points");
        if(points != null){
            for (Map<String, Object> point : points) {
                Double value = (Double) point.get("sog");
                if (value != null && value.doubleValue() > maxValue) {
                    maxValue = value.doubleValue();
                }
            }
        }
        
        return new MaxSpeedRecording(maxValue);
    }

}
