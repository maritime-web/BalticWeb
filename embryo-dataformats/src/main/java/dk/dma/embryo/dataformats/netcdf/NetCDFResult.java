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
import java.util.List;
import java.util.Map;

public class NetCDFResult {
    private Map<String, List<? extends Serializable>> metadata;
    private Map<String, List<SmallEntry>> data;
    
    public NetCDFResult(Map<String, List<? extends Serializable>> metadata, Map<String, List<SmallEntry>> data) {
        this.metadata = metadata;
        this.data = data;
    }
    
    public Map<String, List<? extends Serializable>> getMetadata() {
        return metadata;
    }
    
    public Map<String, List<SmallEntry>> getData() {
        return data;
    }
}
