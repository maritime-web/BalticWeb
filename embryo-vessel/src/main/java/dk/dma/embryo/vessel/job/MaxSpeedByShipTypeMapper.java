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

public class MaxSpeedByShipTypeMapper {
    
    /*
     * 
     * Ais View Types
    Type: Unknown
    Type: Tanker
    Type: Undefined
    Type: Pleasure
    Type: Towing Long/Wide
    Type: Cargo
    Type: Tug
    Type: Sar
    Type: Pilot
    Type: Military
    Type: Fishing
    Type: Passenger
    Type: Towing
    Type: HSC
    Type: Dredging
    Type: Sailing
    Type: WIG
    Type: Diving
    Type: Port tender
    Type: Law enforcement
    Type: Anti pollution
    Type: Ships according to rr
    Type: Medical
    */
    
    /*
    Passenger(19.5),
    Cargo(15.1),
    Tanker(13.6),
    HighSpeedCraft_WIG(),
    Fishing(11.5),
    Sailing_Pleasure(15.0),
    Sailing(6.0),
    SAR_Military_LawEnforcement(25.0),
    Other(12.0);*/
    
    public static final double OTHER                          = 12.0;
    public static final double TANKER                         = 13.6;
    public static final double SAILING_PLEASURE               = 15.0;
    public static final double CARGO                          = 15.1;
    public static final double SAR_MILITARY_LAWENVORCEMENT    = 25.0;
    public static final double FISHING                        = 11.5;
    public static final double PASSENGER                      = 19.5;
    public static final double SAILING                        =  6.0;
    public static final double WIG                            = 40.0;
    public static final double NO_MATCH                       =  0.0;
    
    public static double mapAisShipTypeToMaxSpeed(String aisShipType) {
        
        if(aisShipType == null) {
            return NO_MATCH;
        }
        
        switch (aisShipType) {
            case "Tanker":                  return TANKER;
            case "Pleasure":                return SAILING_PLEASURE;
            case "Towing Long/Wide":        return OTHER;
            case "Cargo":                   return CARGO;
            case "Tug":                     return OTHER;
            case "Sar":                     return SAR_MILITARY_LAWENVORCEMENT;
            case "Pilot":                   return OTHER;
            case "Military":                return SAR_MILITARY_LAWENVORCEMENT;
            case "Fishing":                 return FISHING;
            case "Passenger":               return PASSENGER;
            case "Towing":                  return OTHER;
            case "HSC":                     return OTHER;
            case "Dredging":                return OTHER;
            case "Sailing":                 return SAILING;
            case "WIG":                     return WIG;
            case "Diving":                  return OTHER;
            case "Port tender":             return OTHER;
            case "Law enforcement":         return SAR_MILITARY_LAWENVORCEMENT;
            case "Anti pollution":          return OTHER;
            case "Ships according to rr":   return OTHER;
            case "Medical":                 return OTHER;
            case "Unknown":                 return OTHER;
            case "Undefined":               return OTHER;
            
            default:                        return NO_MATCH;
        }
    }
}
