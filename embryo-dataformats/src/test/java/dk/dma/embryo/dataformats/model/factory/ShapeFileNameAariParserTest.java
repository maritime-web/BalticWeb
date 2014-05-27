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
