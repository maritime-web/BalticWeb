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
