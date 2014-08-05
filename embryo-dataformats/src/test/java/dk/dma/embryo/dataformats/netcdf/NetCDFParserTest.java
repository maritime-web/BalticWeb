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
