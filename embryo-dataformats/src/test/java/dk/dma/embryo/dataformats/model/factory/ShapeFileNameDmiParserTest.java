/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
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
        ShapeFileMeasurement measurement = parser.parse("201412012345_CapeFarewell_RIC");
        
        Assert.assertEquals("dmi", measurement.getProvider());
        Assert.assertEquals("201412012345_CapeFarewell_RIC", measurement.getFileName());
        Assert.assertEquals(0, measurement.getVersion());
        
    }

    @Test
    public void testParseNameWithVersion() {
        ShapeFileMeasurement measurement = parser.parse("201412012345_CapeFarewell_RIC_v2");
        
        Assert.assertEquals("dmi", measurement.getProvider());
        Assert.assertEquals("201412012345_CapeFarewell_RIC", measurement.getFileName());
        Assert.assertEquals(2, measurement.getVersion());
        
    }
}
