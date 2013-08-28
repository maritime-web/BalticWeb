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

    // Properties relevant for current functionality. Extra can be added.

    private String name;

    private Long mmsi;

    private String callSign;

    private String maritimeId;

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public Ship(){
        super();
    }
    
    public Ship(String maritimeId, String name, Long mmsi, String callSign) {
        super();
        this.name = name;
        this.mmsi = mmsi;
        this.callSign = callSign;
        this.maritimeId = maritimeId;
    }

    // //////////////////////////////////////////////////////////////////////
    // Object methods
    // //////////////////////////////////////////////////////////////////////
    @Override
    public String toString(){
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

}
