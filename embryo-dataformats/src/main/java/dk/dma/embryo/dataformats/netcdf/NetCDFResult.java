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

/**
 * The NetCDFResult is what ends up being serialized as JSON data. It consists
 * of user friendly names for the variables, metadata (lat, lon and time) and
 * the actual data points in relation to the variables and metadata.
 * 
 * @author avlund
 *
 */
public class NetCDFResult {
    private Map<String, Integer> variables;
    private Map<String, List<? extends Serializable>> metadata;
    private Map<Integer, NetCDFMoment> data;

    public NetCDFResult(Map<String, Integer> variables, Map<String, List<? extends Serializable>> metadata, Map<Integer, NetCDFMoment> data) {
        this.variables = variables;
        this.metadata = metadata;
        this.data = data;
    }

    public Map<String, Integer> getVariables() {
        return variables;
    }

    public Map<String, List<? extends Serializable>> getMetadata() {
        return metadata;
    }

    public Map<Integer, NetCDFMoment> getData() {
        return data;
    }

    @Override
    public String toString() {
        return "NetCDFResult: Variables: " + variables.toString() + ", Metadata: " + metadata.toString();
    }
}
