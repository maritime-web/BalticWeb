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

import org.junit.Assert;
import org.junit.Test;

public class MaxSpeedByShipTypeMapperTest {

    @Test
    public void test() {
        
        // See types in MaxSpeedByShipTypeMapper.java
        
        Assert.assertTrue(MaxSpeedByShipTypeMapper.mapAisShipTypeToMaxSpeed("Tanker") == MaxSpeedByShipTypeMapper.TANKER);
        Assert.assertTrue(MaxSpeedByShipTypeMapper.mapAisShipTypeToMaxSpeed("Pleasure") == MaxSpeedByShipTypeMapper.SAILING_PLEASURE);
        Assert.assertTrue(MaxSpeedByShipTypeMapper.mapAisShipTypeToMaxSpeed("Towing Long/Wide") == MaxSpeedByShipTypeMapper.OTHER);
        Assert.assertTrue(MaxSpeedByShipTypeMapper.mapAisShipTypeToMaxSpeed("Cargo") == MaxSpeedByShipTypeMapper.CARGO);
        Assert.assertTrue(MaxSpeedByShipTypeMapper.mapAisShipTypeToMaxSpeed("Tug") == MaxSpeedByShipTypeMapper.OTHER);
        Assert.assertTrue(MaxSpeedByShipTypeMapper.mapAisShipTypeToMaxSpeed("Sar") == MaxSpeedByShipTypeMapper.SAR_MILITARY_LAWENVORCEMENT);
        Assert.assertTrue(MaxSpeedByShipTypeMapper.mapAisShipTypeToMaxSpeed("Pilot") == MaxSpeedByShipTypeMapper.OTHER);
        Assert.assertTrue(MaxSpeedByShipTypeMapper.mapAisShipTypeToMaxSpeed("Military") == MaxSpeedByShipTypeMapper.SAR_MILITARY_LAWENVORCEMENT);
        Assert.assertTrue(MaxSpeedByShipTypeMapper.mapAisShipTypeToMaxSpeed("Fishing") == MaxSpeedByShipTypeMapper.FISHING);
        Assert.assertTrue(MaxSpeedByShipTypeMapper.mapAisShipTypeToMaxSpeed("Passenger") == MaxSpeedByShipTypeMapper.PASSENGER);
        Assert.assertTrue(MaxSpeedByShipTypeMapper.mapAisShipTypeToMaxSpeed("Towing") == MaxSpeedByShipTypeMapper.OTHER);
        Assert.assertTrue(MaxSpeedByShipTypeMapper.mapAisShipTypeToMaxSpeed("HSC") == MaxSpeedByShipTypeMapper.OTHER);
        Assert.assertTrue(MaxSpeedByShipTypeMapper.mapAisShipTypeToMaxSpeed("Dredging") == MaxSpeedByShipTypeMapper.OTHER);
        Assert.assertTrue(MaxSpeedByShipTypeMapper.mapAisShipTypeToMaxSpeed("Sailing") == MaxSpeedByShipTypeMapper.SAILING);
        Assert.assertTrue(MaxSpeedByShipTypeMapper.mapAisShipTypeToMaxSpeed("WIG") == MaxSpeedByShipTypeMapper.WIG);
        Assert.assertTrue(MaxSpeedByShipTypeMapper.mapAisShipTypeToMaxSpeed("Diving") == MaxSpeedByShipTypeMapper.OTHER);
        Assert.assertTrue(MaxSpeedByShipTypeMapper.mapAisShipTypeToMaxSpeed("Port tender") == MaxSpeedByShipTypeMapper.OTHER);
        Assert.assertTrue(MaxSpeedByShipTypeMapper.mapAisShipTypeToMaxSpeed("Law enforcement") == MaxSpeedByShipTypeMapper.SAR_MILITARY_LAWENVORCEMENT);
        Assert.assertTrue(MaxSpeedByShipTypeMapper.mapAisShipTypeToMaxSpeed("Anti pollution") == MaxSpeedByShipTypeMapper.OTHER);
        Assert.assertTrue(MaxSpeedByShipTypeMapper.mapAisShipTypeToMaxSpeed("Ships according to rr") == MaxSpeedByShipTypeMapper.OTHER);
        Assert.assertTrue(MaxSpeedByShipTypeMapper.mapAisShipTypeToMaxSpeed("Medical") == MaxSpeedByShipTypeMapper.OTHER);
        Assert.assertTrue(MaxSpeedByShipTypeMapper.mapAisShipTypeToMaxSpeed("Unknown") == MaxSpeedByShipTypeMapper.OTHER);
        Assert.assertTrue(MaxSpeedByShipTypeMapper.mapAisShipTypeToMaxSpeed("Undefined") == MaxSpeedByShipTypeMapper.OTHER);

        Assert.assertTrue(MaxSpeedByShipTypeMapper.mapAisShipTypeToMaxSpeed(null) == MaxSpeedByShipTypeMapper.NO_MATCH);
        Assert.assertTrue(MaxSpeedByShipTypeMapper.mapAisShipTypeToMaxSpeed("RuSkAmSnUsK") == MaxSpeedByShipTypeMapper.NO_MATCH);
        
    }
}
