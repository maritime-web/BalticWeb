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
package dk.dma.embryo.vessel.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dk.dma.embryo.vessel.integration.AisVessel;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.util.Map;

/**
 * 
 * @author Jesper Tejlgaard
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class VesselDetails {
    
    /** Ship name */
    private Long mmsi;

    private String maritimeId;

    /** Communication capabilities */
    private String commCapabilities;

    private Float maxSpeed;

    /** Gross tonnage */
    private Integer grossTon;

    /** Maximum Capacity for persons on board */
    private Integer maxPersons;

    private String iceClass;

    private Boolean helipad;

    private Map<String, Object> additionalInformation;
    
    private AisVessel aisVessel;
    

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public VesselDetails() {
        super();
    }

    // //////////////////////////////////////////////////////////////////////
    // Business Logic
    // //////////////////////////////////////////////////////////////////////

    public Long getMmsiNumber() {
        if (getMmsi() != null) {
            return getMmsi();
        }

        if (getAisVessel() == null) {
            return null;
        }

        return getAisVessel().getMmsi();
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
        result = prime * result + ((mmsi == null)                   ? 0 : mmsi.hashCode());
        result = prime * result + ((maritimeId == null)             ? 0 : maritimeId.hashCode());
        result = prime * result + ((commCapabilities == null)       ? 0 : commCapabilities.hashCode());
        result = prime * result + ((maxSpeed == null)               ? 0 : maxSpeed.hashCode());
        result = prime * result + ((grossTon == null)               ? 0 : grossTon.hashCode());
        result = prime * result + ((maxPersons == null)             ? 0 : maxPersons.hashCode());
        result = prime * result + ((iceClass == null)               ? 0 : iceClass.hashCode());
        result = prime * result + ((helipad == null)                ? 0 : helipad.hashCode());
        result = prime * result + ((additionalInformation == null)  ? 0 : additionalInformation.hashCode());
        result = prime * result + ((aisVessel == null)              ? 0 : aisVessel.hashCode());
        
        return result;
    }
    
    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////

    public Long getMmsi() {
        return mmsi;
    }
    public void setMmsi(Long vesselMmsi) {
        this.mmsi = vesselMmsi;
    }

    public String getMaritimeId() {
        return maritimeId;
    }
    public void setMaritimeId(String vesselMaritimeId) {
        this.maritimeId = vesselMaritimeId;
    }


    public String getCommCapabilities() {
        return commCapabilities;
    }
    public void setCommCapabilities(String commCapabilities) {
        this.commCapabilities = commCapabilities;
    }

    public Float getMaxSpeed() {
        return maxSpeed;
    }
    public void setMaxSpeed(Float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public Integer getGrossTon() {
        return grossTon;
    }
    public void setGrossTon(Integer tonnage) {
        this.grossTon = tonnage;
    }

    public String getIceClass() {
        return iceClass;
    }
    public void setIceClass(String iceClass) {
        this.iceClass = iceClass;
    }

    public Boolean getHelipad() {
        return helipad;
    }
    public void setHelipad(Boolean helipad) {
        this.helipad = helipad;
    }
    
    public Integer getMaxPersons() {
        return maxPersons;
    }
    public void setMaxPersons(Integer maxPersons) {
        this.maxPersons = maxPersons;
    }

    public Map<String, Object> getAdditionalInformation() {
        return additionalInformation;
    }
    public void setAdditionalInformation(Map<String, Object> additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    public AisVessel getAisVessel() {
        return aisVessel;
    }
    public void setAisVessel(AisVessel aisVessel) {
        this.aisVessel = aisVessel;
    }
}
