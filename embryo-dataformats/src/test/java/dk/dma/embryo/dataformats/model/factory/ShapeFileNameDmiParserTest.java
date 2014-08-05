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
package dk.dma.embryo.dataformats.model.factory;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dk.dma.embryo.dataformats.model.ShapeFileMeasurement;

/**
 * @author Jesper Tejlgaard
 */
public class ShapeFileNameDmiParserTest {
    
    private ShapeFileNameDmiParser parser; 
    
    @Before
    public void setup(){
        Map<String, String> regions = new HashMap<>();
        regions.put("CapeFarewell_RIC", "CFW");
        parser = new ShapeFileNameDmiParser(regions);
    }

    @Test
    public void testParseNameNoVersion() {
        ShapeFileMeasurement measurement = parser.parse("iceChart", "201412012345_CapeFarewell_RIC");
        
        Assert.assertEquals("dmi", measurement.getProvider());
        Assert.assertEquals("201412012345_CapeFarewell_RIC", measurement.getFileName());
        Assert.assertEquals(0, measurement.getVersion());
        
    }

    @Test
    public void testParseNameWithVersion() {
        ShapeFileMeasurement measurement = parser.parse("iceChart", "201412012345_CapeFarewell_RIC_v2");
        
        Assert.assertEquals("dmi", measurement.getProvider());
        Assert.assertEquals("201412012345_CapeFarewell_RIC", measurement.getFileName());
        Assert.assertEquals(2, measurement.getVersion());
        
    }
}
