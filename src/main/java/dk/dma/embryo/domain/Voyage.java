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
package dk.dma.embryo.domain;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.joda.time.LocalDateTime;

@Entity
public class Voyage extends BaseEntity<Long> {

    private static final long serialVersionUID = -7205030526506222850L;

    // //////////////////////////////////////////////////////////////////////
    // Entity fields (also see super class)
    // //////////////////////////////////////////////////////////////////////
    private String businessId;
    
    private String berthName;

    private Position position;
    
    private LocalDateTime arrival;

    private LocalDateTime departure;
    
    @ManyToOne
    VoyageInformation2 info;
    
    // //////////////////////////////////////////////////////////////////////
    // business logic
    // //////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
        return "Voyage [" + baseToString() + ", businessId=" + businessId + ", berthName=" + berthName + ", position=" + position + ", arrival=" + arrival + ", departure="
                + departure + "]";
    }

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public Voyage(String key) {
        this.businessId = key;
        position = new Position();
    }

    public Voyage() {
        this(UUID.randomUUID().toString());
    }

    public Voyage(String name, String lattitude, String longitude, LocalDateTime arrival, LocalDateTime departure) {
        this();
        this.berthName = name;
        this.position.setLattitude(lattitude);
        this.position.setLongitude(longitude);
        this.arrival = arrival;
        this.departure = departure;
    }
    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public LocalDateTime getArrival() {
        return arrival;
    }

    public void setArrival(LocalDateTime arrival) {
        this.arrival = arrival;
    }

    public LocalDateTime getDeparture() {
        return departure;
    }

    public void setDeparture(LocalDateTime departure) {
        this.departure = departure;
    }

    public String getBerthName() {
        return berthName;
    }

    public void setBerthName(String berthName) {
        this.berthName = berthName;
    }

    public Position getPosition() {
        return position;
    }
    
    public void setPosition(Position position){
        this.position = position;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String key) {
        if(key == null || key.length() == 0){
            return;
        }
        this.businessId = key;
    }
}
