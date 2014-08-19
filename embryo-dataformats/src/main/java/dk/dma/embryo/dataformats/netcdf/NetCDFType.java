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

public enum NetCDFType {
    WAVE_PROGNOSIS(NetCDFParser.WAVE_HEIGHT, "Wave prognosis"), ICE_PROGNOSIS(NetCDFParser.ICE_CONCENTRATION, "Ice prognosis");
    
    private String trigger;
    private String description;
    
    private NetCDFType(String trigger, String description) {
        this.trigger = trigger;
        this.description = description;
    }
    
    public static NetCDFType triggers(String test) {
        for(NetCDFType val : values()) {
            if(val.trigger.equals(test)) {
                return val;
            }
        }
        return null;
    }
    
    public String getDescription() {
        return description;
    }
}
