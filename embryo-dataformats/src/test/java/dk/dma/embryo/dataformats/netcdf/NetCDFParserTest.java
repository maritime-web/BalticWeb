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

import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NetCDFParserTest {
    @Test
    @Ignore
    public void readNetCDFFile() throws Exception {
        NetCDFParser parser = new NetCDFParser();
        URL resource = getClass().getResource("/netcdf/wam.grib.2014042900.NATLANT.nc");
        NetCDFResult result = parser.parse(resource.getPath());
        
        Map<String, List<? extends Serializable>> metadata = result.getMetadata();
        List<? extends Serializable> latList = metadata.get(NetCDFParser.LAT);
        List<? extends Serializable> lonList = metadata.get(NetCDFParser.LON);
        List<? extends Serializable> timeList = metadata.get(NetCDFParser.TIME);
        assertEquals(97, latList.size());
        assertEquals(199, lonList.size());
        assertEquals(133, timeList.size());
        
        Map<String, List<SmallEntry>> data = result.getData();
        List<SmallEntry> windSpeed = data.get("Wind speed");
        assertEquals(63576, windSpeed.size());
    }
    
    @Test
    @Ignore
    public void readNetCDFFileWithRestrictions() throws Exception {
        NetCDFParser parser = new NetCDFParser();
        URL resource = getClass().getResource("/netcdf/wam.grib.2014042900.NATLANT.nc");
        
        NetCDFRestriction restriction = new NetCDFRestriction();
        restriction.setTimeStart(12);
        restriction.setTimeInterval(24);
        restriction.setMinLat(30);
        restriction.setMaxLat(70);
        restriction.setMinLon(30);
        restriction.setMaxLon(70);
        NetCDFResult result = parser.parse(resource.getPath(), restriction);
        
        Map<String, List<SmallEntry>> data = result.getData();
        List<SmallEntry> windSpeed = data.get("Wind speed");
        assertEquals(9534, windSpeed.size());
    }
}
