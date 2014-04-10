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

import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * 
 * @author Jesper Tejlgaard
 */
public class Voyage {

    // Properties relevant for current functionality. Extra can be added.

    private String maritimeId;

    private String location;

    private Double latitude;

    private Double longitude;

    private Date arrival;

    private Date departure;

    private Integer crew;

    private Integer passengers;

    private Boolean doctor;
    
    private RouteOverview route;

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public Voyage() {
        super();
    }
    
    public Voyage(String maritimeId, String location, Double latitude, Double longitude, Date arrival,
            Date departure, Integer crew, Integer passengers) {
        super();
        this.maritimeId = maritimeId;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.arrival = arrival;
        this.departure = departure;
        this.crew = crew;
        this.passengers = passengers;
    }

    public Voyage(String maritimeId, String location, Double latitude, Double longitude, Date arrival,
            Date departure, Integer crew, Integer passengers, Boolean doctor) {
        super();
        this.maritimeId = maritimeId;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.arrival = arrival;
        this.departure = departure;
        this.crew = crew;
        this.passengers = passengers;
        this.doctor = doctor;
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

    public String getMaritimeId() {
        return maritimeId;
    }

    public void setMaritimeId(String maritimeId) {
        this.maritimeId = maritimeId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

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

    public Date getArrival() {
        return arrival;
    }

    public void setArrival(Date arrival) {
        this.arrival = arrival;
    }

    public Date getDeparture() {
        return departure;
    }

    public void setDeparture(Date departure) {
        this.departure = departure;
    }

    public Integer getCrew() {
        return crew;
    }

    public void setCrew(Integer crew) {
        this.crew = crew;
    }

    public Integer getPassengers() {
        return passengers;
    }

    public void setPassengers(Integer passengers) {
        this.passengers = passengers;
    }

    public Boolean isDoctor() {
        return doctor;
    }

    public void setDoctor(Boolean doctor) {
        this.doctor = doctor;
    }
    
    public RouteOverview getRoute() {
        return route;
    }

    public void setRoute(RouteOverview route) {
        this.route = route;
    }
    
    public static class RouteOverview{
        
        private String id;
        private String name;
        private String dep;
        private String des;
        private Integer waypointCount;
        
        public RouteOverview(String id, String name, String dep, String des, Integer waypointCount) {
            super();
            this.id = id;
            this.name = name;
            this.dep = dep;
            this.des = des;
            this.waypointCount = waypointCount;
        }
        public String getId() {
            return id;
        }
        public String getName() {
            return name;
        }
        public String getDep() {
            return dep;
        }
        public String getDes() {
            return des;
        }
        public Integer getWaypointCount() {
            return waypointCount;
        }
    }
}
