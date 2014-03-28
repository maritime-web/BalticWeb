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
package dk.dma.embryo.vessel.model;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

import dk.dma.embryo.common.util.ParseUtils;

@Embeddable
@Access(AccessType.FIELD)
public class Position implements Serializable {
    
    private static final long serialVersionUID = 249219940778068392L;

    // //////////////////////////////////////////////////////////////////////
    // Entity fields (also see super class)
    // //////////////////////////////////////////////////////////////////////
    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    // //////////////////////////////////////////////////////////////////////
    // business logic
    // //////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////

    public static Position fromEnavModel(dk.dma.enav.model.geometry.Position pos){
        return new Position(pos.getLatitude(), pos.getLongitude());
    }
    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public Position(){
        
    }

    public Position(String latitude, String longitude) {
        this.latitude = latitude.length() == 0 ? null : ParseUtils.parseLatitude(latitude);
        this.longitude = longitude.length() == 0 ? null : ParseUtils.parseLongitude(longitude);
    }

    public Position(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    
    public dk.dma.enav.model.geometry.Position asGeometryPosition(){
        return dk.dma.enav.model.geometry.Position.create(latitude, longitude);
    }
    
    public String getLatitudeAsString(){
        if(latitude == null){
            return null;
        }
        return dk.dma.enav.model.geometry.Position.create(latitude, longitude).getLatitudeAsString();
    }
    
    public String getLongitudeAsString(){
        if(longitude == null){
            return null;
        }
        return dk.dma.enav.model.geometry.Position.create(latitude, longitude).getLongitudeAsString();
    }
    
}
