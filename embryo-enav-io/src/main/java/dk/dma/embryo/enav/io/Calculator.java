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
package dk.dma.embryo.enav.io;

import dk.dma.enav.model.geometry.Position;
import dk.dma.enav.model.voyage.RouteLeg.Heading;

/**
 * @author Jesper Tejlgaard
 */
public class Calculator {

    private static final int NM_IN_METERS = 1852;

    public static double distanceInNm(double fromLat, double fromLon, Heading heading, double toLat, double toLon){
        Position pos1 = Position.create(fromLat, fromLon);
        Position pos2 = Position.create(toLat, toLon);
        
        double meters = (heading == Heading.RL) ? pos1.rhumbLineDistanceTo(pos2) : pos1.geodesicDistanceTo(pos2);
        
        return metersToNm(meters);
    }
    
    public static double metersToNm(double meters) {
        return meters / NM_IN_METERS;
    }
    
    public static double metersPerSecond2Knots(double metersPerSecond){
        return metersPerSecond * 1.943844;
    }

}
