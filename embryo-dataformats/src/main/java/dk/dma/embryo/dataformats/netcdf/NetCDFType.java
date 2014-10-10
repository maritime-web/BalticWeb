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

import java.util.HashMap;
import java.util.Map;

public abstract class NetCDFType {

    private String name;
    
    private String code;

    private Map<String, NetCDFVar> vars = new HashMap<>();
    
    protected void setName(String name) {
        this.name = name;
    }
    
    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }
    
    public String getCode() {
        return code;
    }

    public Map<String, NetCDFVar> getVars() {
        return vars;
    }

    @Override
    public String toString() {
        return name + " (" + code + "), vars: " + vars;
    }
}
