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
package dk.dma.arcticweb.reporting.model;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * 
 * @author Jesper Tejlgaard
 */
public class GreenposMinimal implements Serializable{

    private static final long serialVersionUID = -7205030526506222850L;

    // //////////////////////////////////////////////////////////////////////
    // Class fields (also see super class)
    // //////////////////////////////////////////////////////////////////////
    private String name;
    private Long mmsi;
    private String type;
    private Date ts;
    
    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public GreenposMinimal(String name, Long mmsi, String type, Date ts) {
        super();
        this.name = name;
        this.mmsi = mmsi;
        this.type = type;
        this.ts = ts;
    }
    // //////////////////////////////////////////////////////////////////////
    // Object methods
    // //////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result + ((mmsi == null) ? 0 : mmsi.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((ts == null) ? 0 : ts.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        
        return result;
    }

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public Date getTs() {
        return ts;
    }
    public void setTs(Date ts) {
        this.ts = ts;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Long getMmsi() {
        return mmsi;
    }
    public void setMmsi(Long mmsi) {
        this.mmsi = mmsi;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

}
