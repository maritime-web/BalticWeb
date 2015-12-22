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
import static org.hamcrest.core.Is.is;
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
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(TANKER), is(13.6d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypePleasure() {
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(PLEASURE), is(15.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeTowingLongWide() {
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(TOWING_LONG_WIDE), is(12.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeCargo() {
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(CARGO), is(15.1d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeTug() {
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(TUG), is(12.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeSar() {
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(SAR), is(25.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypePilot() {
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(PILOT), is(12.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeMilitary() {
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(MILITARY), is(25.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeFishing() {
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(FISHING), is(11.5d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypePassenger() {
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(PASSENGER), is(19.5d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeTowing() {
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(TOWING), is(12.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeHsc() {
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(HSC), is(12.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeDredging() {
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(DREDGING), is(12.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeSailing() {
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(SAILING), is(6.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeWig() {
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(WIG), is(40.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeDiving() {
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(DIVING), is(12.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypePortTender() {
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(PORT_TENDER), is(12.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeLawEnforcement() {
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(LAW_ENFORCEMENT), is(25.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeAntiPollution() {
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(ANTI_POLLUTION), is(12.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeShipsAccordingToRr() {
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(SHIPS_ACCORDING_TO_RR), is(12.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeMedical() {
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(MEDICAL), is(12.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeUnknown() {
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(UNKNOWN), is(12.0d));
    }

    @Test
    public void testLookupSpeedForAisVesselTypeUndefined() {
        assertThat(ServiceSpeedByShipTypeMapper.lookupSpeed(UNDEFINED), is(12.0d));
    }
}
