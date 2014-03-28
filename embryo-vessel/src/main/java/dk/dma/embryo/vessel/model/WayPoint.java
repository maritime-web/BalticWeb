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

import javax.persistence.Embeddable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import dk.dma.enav.model.voyage.Waypoint;

@Embeddable
public class WayPoint implements Serializable {

    private static final long serialVersionUID = -7205030526506222850L;

    // //////////////////////////////////////////////////////////////////////
    // Entity fields (also see super class)
    // //////////////////////////////////////////////////////////////////////

    @NotNull
    @Size(min=1)
    private String name;

    @Valid
    private Position position;
    
    /** Rate of turn. */
    private Double rot;

    /** Waypoint turn radius in nautical miles. */
    private Double turnRadius;
    
    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime eta;
    
    @Valid
    private RouteLeg leg;

    // //////////////////////////////////////////////////////////////////////
    // business logic
    // //////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////

    public static WayPoint fromJsonModel(dk.dma.embryo.vessel.json.Waypoint wayPoint){
        WayPoint transformed = new WayPoint();
        transformed.setName(wayPoint.getName());
        transformed.setPosition(new Position(wayPoint.getLatitude(), wayPoint.getLongitude()));
        transformed.setRot(wayPoint.getRot());
        transformed.setTurnRadius(wayPoint.getTurnRad());
        
        transformed.setLeg(RouteLeg.fromJsonModel(wayPoint));
        
        return transformed;
    }
    
    public dk.dma.embryo.vessel.json.Waypoint toJsonModel(){
        dk.dma.embryo.vessel.json.Waypoint toWaypoint = new dk.dma.embryo.vessel.json.Waypoint();
        toWaypoint.setName(this.getName());
        toWaypoint.setLatitude(this.getPosition().getLatitude());
        toWaypoint.setLongitude(this.getPosition().getLongitude());
        toWaypoint.setRot(this.getRot());
        toWaypoint.setTurnRad(this.getTurnRadius());
        
        if(this.getLeg() != null){
            toWaypoint.setSpeed(this.getLeg().getSpeed());
            toWaypoint.setHeading(this.getLeg().getHeading());
        }
        
        return toWaypoint;
    }
    
    
    public static WayPoint fromEnavModel(Waypoint wayPoint){
        WayPoint transformed = new WayPoint();
        transformed.setName(wayPoint.getName());
        transformed.setPosition(new Position(wayPoint.getLatitude(), wayPoint.getLongitude()));
        transformed.setRot(wayPoint.getRot());
        transformed.setTurnRadius(wayPoint.getTurnRad());
        
        transformed.setLeg(RouteLeg.fromEnavModel(wayPoint.getRouteLeg()));
        
        return transformed;
    }
    
    public Waypoint toEnavModel(){
        Waypoint toWaypoint = new Waypoint();
        toWaypoint.setName(this.getName());
        toWaypoint.setLatitude(this.getPosition().getLatitude());
        toWaypoint.setLongitude(this.getPosition().getLongitude());
        toWaypoint.setRot(this.getRot());
        toWaypoint.setTurnRad(this.getTurnRadius());
        toWaypoint.setEta(getEta() == null ? null : getEta().toDate());
        
        if(this.getLeg() != null){
            toWaypoint.setRouteLeg(this.getLeg().toEnavModel());
        }
        
        return toWaypoint;
    }
    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public WayPoint() {
        position = new Position();
    }

    public WayPoint(String name, Double latitude, Double longitude, Double rot, Double turnRadius) {
        this();
        
        this.name = name;
        position.setLatitude(latitude);
        position.setLongitude(longitude);
        this.rot = rot;
        this.turnRadius = turnRadius;
    }

    // //////////////////////////////////////////////////////////////////////
    // Object methods
    // //////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
        return "WayPoint [name=" + name + ", position=" + position + ", turnRadius=" + turnRadius + "]";
    }

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public String getName() {
        return name;
    }

    public Position getPosition() {
        return position;
    }

    public Double getTurnRadius() {
        return turnRadius;
    }

    public RouteLeg getLeg() {
        return leg;
    }

    public Double getRot() {
        return rot;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLeg(RouteLeg leg) {
        this.leg = leg;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void setRot(Double rot) {
        this.rot = rot;
    }

    public void setTurnRadius(Double turnRadius) {
        this.turnRadius = turnRadius;
    }

    public DateTime getEta() {
        return eta;
    }

    public void setEta(DateTime eta) {
        this.eta = eta;
    }
}
