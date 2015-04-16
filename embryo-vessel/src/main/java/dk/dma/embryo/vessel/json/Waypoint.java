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

import dk.dma.enav.model.voyage.RouteLeg.Heading;

public class Waypoint {

    /** Waypoint name */
    private String name;

    /** Mandatory latitude. */
    private double latitude;
    /** Mandatory longitude. */
    private double longitude;
    
    /** Speed in knots. */
    private Double speed;

    /** Sail heading - rhumb line or great circle */
    private Heading heading;

    /** Rate of turn. */
    private Double rot;

    /** Waypoint turn radius in nautical miles. */
    private Double turnRad;
    
    private Date eta;

    public Waypoint() {

    }

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public Waypoint(String name, double latitude, double longitude, Double rot, Double turnRad) {
        super();
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rot = rot;
        this.turnRad = turnRad;
    }
    
    
    public Waypoint(String name, double latitude, double longitude, Double speed, Heading heading, Double rot, Double turnRad, Date eta) {
        this(name, latitude, longitude, rot, turnRad);
        this.speed = speed;
        this.heading = heading;
        this.eta = eta;
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
    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }

    public Double getRot() {
        return rot;
    }

    public Double getTurnRad() {
        return turnRad;
    }

    public Double getSpeed() {
        return speed;
    }

    public Heading getHeading() {
        return heading;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRot(Double rot) {
        this.rot = rot;
    }

    public void setTurnRad(Double turnRad) {
        this.turnRad = turnRad;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public void setHeading(Heading heading) {
        this.heading = heading;
    }

    public Date getEta() {
        return eta;
    }

    public void setEta(Date eta) {
        this.eta = eta;
    }
}
