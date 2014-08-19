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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dk.dma.embryo.dataformats.model.ShapeFileMeasurement;

/**
 * @author Jesper Tejlgaard
 */
public class ShapeFileNameAariParserTest {
    
    private ShapeFileNameAariParser parser; 
    
    @Before
    public void setup(){
        parser = new ShapeFileNameAariParser();
    }

    @Test
    public void testParseNameNoVersion() {
        ShapeFileMeasurement measurement = parser.parse("iceChart", "aari_whi_20140408_pl_a");
        
        Assert.assertEquals("aari", measurement.getProvider());
        Assert.assertEquals("aari_whi_20140408_pl_a", measurement.getFileName());
        Assert.assertEquals(0, measurement.getVersion());
        
    }
}
