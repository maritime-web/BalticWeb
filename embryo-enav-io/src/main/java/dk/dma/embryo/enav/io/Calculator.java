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
