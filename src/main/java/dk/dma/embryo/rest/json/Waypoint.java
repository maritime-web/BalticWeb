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

    /** Sail heading rhumb line or great circle */
    private Heading heading;

    /** Rate of turn . */
    private Double rot;

    /** Waypoint turn radius in nautical miles. */
    private Double turnRad;

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
    
    


}
