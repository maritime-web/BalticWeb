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
package dk.dma.embryo.common.util;

/**
 * Class for doing different conversions
 */
public class Converter {
    
    private static final int NM_IN_METERS = 1852;
    private static final double M_IN_NM = 0.868976242;

    public static double metersToNm(double meters) {
        return meters / NM_IN_METERS;
    }
    
    public static double nmToMeters(double nm) {
        return nm * NM_IN_METERS;
    }
    
    public static double milesToNM(double m) {
        return m * M_IN_NM;
    }
    
    
}
