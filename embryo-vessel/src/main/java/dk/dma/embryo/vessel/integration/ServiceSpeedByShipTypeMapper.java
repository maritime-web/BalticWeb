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
package dk.dma.embryo.vessel.integration;

public class ServiceSpeedByShipTypeMapper {
    
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
    public static final double SAR_MILITARY_LAW_ENFORCEMENT = 25.0;
    public static final double FISHING                        = 11.5;
    public static final double PASSENGER                      = 19.5;
    public static final double SAILING                        =  6.0;
    public static final double WIG                            = 40.0;
    public static final double NO_MATCH                       =  0.0;

    public static double lookupSpeed(VesselType aisVesselType) {

        if (aisVesselType == null) {
            return NO_MATCH;
        }

        switch (aisVesselType) {
            case TANKER:
                return TANKER;
            case PLEASURE:
                return SAILING_PLEASURE;
            case TOWING_LONG_WIDE:
                return OTHER;
            case CARGO:
                return CARGO;
            case TUG:
                return OTHER;
            case SAR:
                return SAR_MILITARY_LAW_ENFORCEMENT;
            case PILOT:
                return OTHER;
            case MILITARY:
                return SAR_MILITARY_LAW_ENFORCEMENT;
            case FISHING:
                return FISHING;
            case PASSENGER:
                return PASSENGER;
            case TOWING:
                return OTHER;
            case HSC:
                return OTHER;
            case DREDGING:
                return OTHER;
            case SAILING:
                return SAILING;
            case WIG:
                return WIG;
            case DIVING:
                return OTHER;
            case PORT_TENDER:
                return OTHER;
            case LAW_ENFORCEMENT:
                return SAR_MILITARY_LAW_ENFORCEMENT;
            case ANTI_POLLUTION:
                return OTHER;
            case SHIPS_ACCORDING_TO_RR:
                return OTHER;
            case MEDICAL:
                return OTHER;
            case UNKNOWN:
                return OTHER;
            case UNDEFINED:
                return OTHER;
            
            default:                        return NO_MATCH;
        }
    }
}
