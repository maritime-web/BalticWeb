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
package dk.dma.embryo.vessel.json;

import java.util.Map;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * 
 * @author Jesper Tejlgaard
 */
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
    
//    private Map<String, Object> reporting;
    
    private Map<String, Object> ais;

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public VesselDetails() {
        super();
    }

    // //////////////////////////////////////////////////////////////////////
    // Object methods
    // //////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
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

    /**
     * @return gross tonnage
     */
    public Integer getGrossTon() {
        return grossTon;
    }

    /**
     * Set gross tonnage
     */
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

    public Map<String, Object> getAis() {
        return ais;
    }

    public void setAis(Map<String, Object> ais) {
        this.ais = ais;
    }

//    public Map<String, Object> getReporting() {
//        return reporting;
//    }
//
//    public void setReporting(Map<String, Object> reporting) {
//        this.reporting = reporting;
//    }
}
