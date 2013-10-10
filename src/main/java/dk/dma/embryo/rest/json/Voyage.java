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
public class Voyage {

    // Properties relevant for current functionality. Extra can be added.

    private String maritimeId;

    private String berthName;

    private String latitude;

    private String longitude;

    private String arrival;

    private String departure;

    private Integer crew;

    private Integer passengers;

    private Boolean doctor;
    
    private String routeId;

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public Voyage() {
        super();
    }
    
    public Voyage(String maritimeId, String berthName, String latitude, String longitude, String arrival,
            String departure, Integer crew, Integer passengers) {
        super();
        this.maritimeId = maritimeId;
        this.berthName = berthName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.arrival = arrival;
        this.departure = departure;
        this.crew = crew;
        this.passengers = passengers;
    }

    public Voyage(String maritimeId, String berthName, String latitude, String longitude, String arrival,
            String departure, Integer crew, Integer passengers, Boolean doctor) {
        super();
        this.maritimeId = maritimeId;
        this.berthName = berthName;
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

    public String getBerthName() {
        return berthName;
    }

    public void setBerthName(String berthName) {
        this.berthName = berthName;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getArrival() {
        return arrival;
    }

    public void setArrival(String arrival) {
        this.arrival = arrival;
    }

    public String getDeparture() {
        return departure;
    }

    public void setDeparture(String departure) {
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

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }
}
