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
package dk.dma.embryo.rest.json;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * 
 * @author Jesper Tejlgaard
 */
public class Ship {

    /** Ship name */
    private String name;

    private Long mmsi;

    private String callSign;

    private String maritimeId;

    private Long imo;

    private String type;

    /** Communication capabilities */
    private String commCapabilities;

    private Integer width;

    private Integer length;

    private Float maxSpeed;

    /** Gross tonnage */
    private Integer grossTon;

    private String iceClass;

    private Boolean helipad;

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public Ship() {
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
    public String getName() {
        return name;
    }

    public void setName(String shipName) {
        this.name = shipName;
    }

    public Long getMmsi() {
        return mmsi;
    }

    public void setMmsi(Long shipMmsi) {
        this.mmsi = shipMmsi;
    }

    public String getCallSign() {
        return callSign;
    }

    public void setCallSign(String shipCallSign) {
        this.callSign = shipCallSign;
    }

    public String getMaritimeId() {
        return maritimeId;
    }

    public void setMaritimeId(String shipMaritimeId) {
        this.maritimeId = shipMaritimeId;
    }

    public Long getImo() {
        return imo;
    }

    public void setImo(Long imo) {
        this.imo = imo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCommCapabilities() {
        return commCapabilities;
    }

    public void setCommCapabilities(String commCapabilities) {
        this.commCapabilities = commCapabilities;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
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

}
