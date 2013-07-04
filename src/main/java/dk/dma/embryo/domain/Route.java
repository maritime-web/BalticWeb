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

import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;

import org.joda.time.LocalDateTime;

@Entity
@NamedQueries({@NamedQuery(name="Route:getByMmsi", query="SELECT DISTINCT r FROM Route r LEFT JOIN FETCH r.wayPoints where r.ship.mmsi = :mmsi")})
public class Route extends BaseEntity<Long> {

    private static final long serialVersionUID = -7205030526506222850L;

    // //////////////////////////////////////////////////////////////////////
    // Entity fields (also see super class)
    // //////////////////////////////////////////////////////////////////////
    private String name;
    
    private String destination;

    private String origin;

    transient private String voyageName;
    
    private LocalDateTime etaOfArrival;

    private LocalDateTime etaOfDeparture;

    @ElementCollection
    @CollectionTable(name="WayPoint")
    private List<WayPoint> wayPoints;
    
    @OneToOne
    private Ship2 ship;
    
    @OneToOne
    Voyage voyage;
    
    // //////////////////////////////////////////////////////////////////////
    // business logic
    // //////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////
    public void addWayPoint(WayPoint wPoint){
        wayPoints.add(wPoint);
    }
    
    
    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public Route() {
    }

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getVoyageName() {
        return voyageName;
    }

    public void setVoyageName(String voyageName) {
        this.voyageName = voyageName;
    }

    public LocalDateTime getEtaOfArrival() {
        return etaOfArrival;
    }

    public void setEtaOfArrival(LocalDateTime etaOfArrival) {
        this.etaOfArrival = etaOfArrival;
    }

    public LocalDateTime getEtaOfDeparture() {
        return etaOfDeparture;
    }

    public void setEtaOfDeparture(LocalDateTime etaOfDeparture) {
        this.etaOfDeparture = etaOfDeparture;
    }

    public List<WayPoint> getWayPoints() {
        return wayPoints;
    }

    public Ship2 getShip() {
        return ship;
    }

    public void setShip(Ship2 ship){
        this.ship = ship;
    }
    
    public Voyage getVoyage() {
        return voyage;
    }
}
