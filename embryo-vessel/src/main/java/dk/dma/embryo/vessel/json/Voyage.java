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

    public Voyage(
            String maritimeId, 
            String location, 
            Double latitude, 
            Double longitude, 
            Date arrival, 
            Date departure, 
            Integer crew,
            Integer passengers) {
        
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

    public Voyage(
            String maritimeId, 
            String location, 
            Double latitude, 
            Double longitude, 
            Date arrival, 
            Date departure, 
            Integer crew,
            Integer passengers, 
            Boolean doctor) {
        
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        
        result = prime * result + ((arrival == null) ? 0 : arrival.hashCode());
        result = prime * result + ((crew == null) ? 0 : crew.hashCode());
        result = prime * result + ((departure == null) ? 0 : departure.hashCode());
        result = prime * result + ((doctor == null) ? 0 : doctor.hashCode());
        result = prime * result + ((latitude == null) ? 0 : latitude.hashCode());
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        result = prime * result + ((longitude == null) ? 0 : longitude.hashCode());
        result = prime * result + ((maritimeId == null) ? 0 : maritimeId.hashCode());
        result = prime * result + ((passengers == null) ? 0 : passengers.hashCode());
        result = prime * result + ((route == null) ? 0 : route.hashCode());
        
        return result;
    }
    
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

    public static class RouteOverview {

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
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            
            result = prime * result + ((dep == null) ? 0 : dep.hashCode());
            result = prime * result + ((des == null) ? 0 : des.hashCode());
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((waypointCount == null) ? 0 : waypointCount.hashCode());
            
            return result;
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
