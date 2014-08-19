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
package dk.dma.embryo.dataformats.netcdf;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class NetCDFParserTest {
    @Test
    public void readNetCDFFile() throws Exception {
        NetCDFParser parser = new NetCDFParser();
        URL resource = getClass().getResource("/netcdf/WAV_2014071400.hh00_test.nc");
        NetCDFResult result = parser.parse(resource.getPath());
        
        Map<String, List<? extends Serializable>> metadata = result.getMetadata();
        List<? extends Serializable> latList = metadata.get(NetCDFParser.LAT);
        List<? extends Serializable> lonList = metadata.get(NetCDFParser.LON);
        List<? extends Serializable> timeList = metadata.get(NetCDFParser.TIME);
        assertEquals(21, latList.size());
        assertEquals(41, lonList.size());
        assertEquals(6, timeList.size());
        
        Map<String, SmallEntry> data = result.getData();
        assertEquals(1056, data.size());
    }
    
    @Test
    public void readNetCDFFileWithRestrictions() throws Exception {
        NetCDFParser parser = new NetCDFParser();
        URL resource = getClass().getResource("/netcdf/WAV_2014071400.hh00_test.nc");
        
        NetCDFRestriction restriction = new NetCDFRestriction();
//        restriction.setTimeStart(12);
//        restriction.setTimeInterval(24);
        restriction.setMinLat(5);
        restriction.setMaxLat(15);
        restriction.setMinLon(5);
        restriction.setMaxLon(35);
        NetCDFResult result = parser.parse(resource.getPath(), restriction);
        
        Map<String, SmallEntry> data = result.getData();
        assertEquals(438, data.size());
    }
}
