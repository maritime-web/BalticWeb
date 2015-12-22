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

import org.hamcrest.core.Is;
import org.junit.Test;

import static dk.dma.embryo.vessel.integration.VesselType.ANTI_POLLUTION;
import static dk.dma.embryo.vessel.integration.VesselType.CARGO;
import static dk.dma.embryo.vessel.integration.VesselType.DIVING;
import static dk.dma.embryo.vessel.integration.VesselType.DREDGING;
import static dk.dma.embryo.vessel.integration.VesselType.FISHING;
import static dk.dma.embryo.vessel.integration.VesselType.HSC;
import static dk.dma.embryo.vessel.integration.VesselType.LAW_ENFORCEMENT;
import static dk.dma.embryo.vessel.integration.VesselType.MEDICAL;
import static dk.dma.embryo.vessel.integration.VesselType.MILITARY;
import static dk.dma.embryo.vessel.integration.VesselType.PASSENGER;
import static dk.dma.embryo.vessel.integration.VesselType.PILOT;
import static dk.dma.embryo.vessel.integration.VesselType.PLEASURE;
import static dk.dma.embryo.vessel.integration.VesselType.PORT_TENDER;
import static dk.dma.embryo.vessel.integration.VesselType.SAILING;
import static dk.dma.embryo.vessel.integration.VesselType.SAR;
import static dk.dma.embryo.vessel.integration.VesselType.SHIPS_ACCORDING_TO_RR;
import static dk.dma.embryo.vessel.integration.VesselType.TANKER;
import static dk.dma.embryo.vessel.integration.VesselType.TOWING;
import static dk.dma.embryo.vessel.integration.VesselType.TOWING_LONG_WIDE;
import static dk.dma.embryo.vessel.integration.VesselType.TUG;
import static dk.dma.embryo.vessel.integration.VesselType.UNDEFINED;
import static dk.dma.embryo.vessel.integration.VesselType.UNKNOWN;
import static dk.dma.embryo.vessel.integration.VesselType.WIG;
import static org.junit.Assert.assertThat;

/**
 * Created by Jesper Tejlgaard on 12/21/15.
 */
public class ServiceSpeedByShipTypeMapperTest {

    /*
    *  case "TANKER":                  return TANKER;
            case "PLEASURE":                return SAILING_PLEASURE;
            case "TOWING LONG/WIDE":        return OTHER;
            case "CARGO":                   return CARGO;
            case "TUG":                     return OTHER;
            case "SAR":                     return SAR_MILITARY_LAW_ENFORCEMENT;
            case "PILOT":                   return OTHER;
            case "MILITARY":                return SAR_MILITARY_LAW_ENFORCEMENT;
            case "FISHING":                 return FISHING;
            case "PASSENGER":               return PASSENGER;
            case "TOWING":                  return OTHER;
            case "HSC":                     return OTHER;
            case "DREDGING":                return OTHER;
            case "SAILING":                 return SAILING;
            case "WIG":                     return WIG;
            case "DIVING":                  return OTHER;
            case "PORT TENDER":             return OTHER;
            case "LAW ENFORCEMENT":         return SAR_MILITARY_LAW_ENFORCEMENT;
            case "ANTI POLLUTION":          return OTHER;
            case "SHIPS ACCORDING TO RR":   return OTHER;
            case "MEDICAL":                 return OTHER;
            case "UNKNOWN":                 return OTHER;
            case "UNDEFINED":               return OTHER;
    * */

    @Test
    public void testLookupSpeedForAisVesselTypeTanker() {
        // expect same result no matter if using upper og lower case letters
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(TANKER), Is.is(13.6d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypePleasure() {
        // expect same result no matter if using upper og lower case letters
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(PLEASURE), Is.is(15.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeTowingLongWide() {
        // expect same result no matter if using upper og lower case letters
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(TOWING_LONG_WIDE), Is.is(12.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeCargo() {
        // expect same result no matter if using upper og lower case letters
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(CARGO), Is.is(15.1d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeTug() {
        // expect same result no matter if using upper og lower case letters
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(TUG), Is.is(12.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeSar() {
        // expect same result no matter if using upper og lower case letters
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(SAR), Is.is(25.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypePilot() {
        // expect same result no matter if using upper og lower case letters
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(PILOT), Is.is(12.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeMilitary() {
        // expect same result no matter if using upper og lower case letters
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(MILITARY), Is.is(25.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeFishing() {
        // expect same result no matter if using upper og lower case letters
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(FISHING), Is.is(11.5d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypePassenger() {
        // expect same result no matter if using upper og lower case letters
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(PASSENGER), Is.is(19.5d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeTowing() {
        // expect same result no matter if using upper og lower case letters
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(TOWING), Is.is(12.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeHsc() {
        // expect same result no matter if using upper og lower case letters
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(HSC), Is.is(12.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeDredging() {
        // expect same result no matter if using upper og lower case letters
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(DREDGING), Is.is(12.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeSailing() {
        // expect same result no matter if using upper og lower case letters
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(SAILING), Is.is(6.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeWig() {
        // expect same result no matter if using upper og lower case letters
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(WIG), Is.is(40.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeDiving() {
        // expect same result no matter if using upper og lower case letters
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(DIVING), Is.is(12.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypePortTender() {
        // expect same result no matter if using upper og lower case letters
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(PORT_TENDER), Is.is(12.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeLawEnforcement() {
        // expect same result no matter if using upper og lower case letters
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(LAW_ENFORCEMENT), Is.is(25.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeAntiPollution() {
        // expect same result no matter if using upper og lower case letters
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(ANTI_POLLUTION), Is.is(12.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeShipsAccordingToRr() {
        // expect same result no matter if using upper og lower case letters
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(SHIPS_ACCORDING_TO_RR), Is.is(12.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeMedical() {
        // expect same result no matter if using upper og lower case letters
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(MEDICAL), Is.is(12.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeUnknown() {
        // expect same result no matter if using upper og lower case letters
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(UNKNOWN), Is.is(12.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeUndefined() {
        // expect same result no matter if using upper og lower case letters
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(UNDEFINED), Is.is(12.0d));
    }
}
