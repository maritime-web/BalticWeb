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

public class ShipTypeCargo {

    public enum ShipType {
        UNDEFINED("Undefined"), 
        WIG("WIG"), 
        PILOT("Pilot"), 
        SAR("Sar"), 
        TUG("Tug"), 
        PORT_TENDER("Port tender"), 
        ANTI_POLLUTION("Anti pollution"), 
        LAW_ENFORCEMENT("Law enforcement"), 
        MEDICAL("Medical"), 
        FISHING("Fishing"), 
        TOWING("Towing"), 
        TOWING_LONG_WIDE("Towing Long/Wide"), 
        DREDGING("Dredging"), 
        DIVING("Diving"), 
        MILITARY("Military"), 
        SAILING("Sailing"), 
        PLEASURE("Pleasure"), 
        HSC("HSC"), 
        PASSENGER("Passenger"), 
        CARGO("Cargo"), 
        TANKER("Tanker"), 
        SHIPS_ACCORDING_TO_RR("Ships according to RR"), 
        UNKNOWN("Unknown");

        private String subType;

        ShipType(String subType) {
            this.subType = subType;
        }

        public static ShipType getShipTypeFromSubType(String subType) {

            for (ShipType shipType : ShipType.values()) {
                if(shipType.subType.equalsIgnoreCase(subType)) {
                    return shipType;
                }
            }

            return UNDEFINED;
        }
    }
}
