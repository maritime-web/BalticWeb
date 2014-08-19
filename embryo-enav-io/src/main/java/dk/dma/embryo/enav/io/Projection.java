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



/**
 * Utility class for converting different projections
 * 
 * @author Jesper Tejlgaard
 */
public enum Projection {

    GOOGLE_MERCATOR;

    public static Position reproject(double latitude, double longitude, Projection projection) {
        if (projection == GOOGLE_MERCATOR) {
            // after http://stackoverflow.com/questions/11957538/converting-geographic-wgs-84-to-web-mercator-102100

            double x = latitude * 1000;
            double y = longitude * 1000;
            double num3 = x / 6378137.0;
            double num4 = num3 * 57.295779513082323;
            double num5 = Math.floor((num4 + 180.0) / 360.0);
            double num6 = num4 - (num5 * 360.0);
            double num7 = 1.5707963267948966 - (2.0 * Math.atan(Math.exp((-1.0 * y) / 6378137.0)));
            double x1 = num6;
            double y1 = num7 * 57.295779513082323;
            return Position.create(x1, y1);
        } 
        
        throw new IllegalArgumentException("Unknown projection");
    }
    
}    
