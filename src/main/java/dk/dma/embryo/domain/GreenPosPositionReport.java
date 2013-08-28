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

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * 
 * @author Jesper Tejlgaard
 */
@Entity
public class GreenPosPositionReport extends GreenPosReport {

    private static final long serialVersionUID = -7205030526506222850L;

    // //////////////////////////////////////////////////////////////////////
    // Entity fields (also see super class)
    // //////////////////////////////////////////////////////////////////////
    @NotNull
    private Double speed;

    @NotNull
    private Integer course;
    
    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////

//    public static Report fromEnavModel(dk.dma.enav.model.voyage.Route from) {
//        Report route = new Report(from.getId(), from.getName(), from.getDeparture(), from.getDestination());
//
//        for (Waypoint wayPoint : from.getWaypoints()) {
//            route.addWayPoint(WayPoint.fromEnavModel(wayPoint));
//        }
//
//        return route;
//    }
//
//    public dk.dma.enav.model.voyage.Route toEnavModel() {
//        dk.dma.enav.model.voyage.Route toRoute = new dk.dma.enav.model.voyage.Route(this.enavId);
//        toRoute.setName(this.name);
//        toRoute.setDeparture(this.origin);
//        toRoute.setDestination(this.destination);
//
//        for (WayPoint wp : this.getWayPoints()) {
//            toRoute.getWaypoints().add(wp.toEnavModel());
//        }
//
//        return toRoute;
//    }

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public GreenPosPositionReport() {
        super();
    }

    
    public GreenPosPositionReport(String shipName, Long shipMmsi, String shipCallSign, String shipMaritimeId,
            String latitude, String longitude, String weather, String iceInformation, Double speed, Integer course) {
        super(shipName, shipMmsi, shipCallSign, shipMaritimeId, latitude, longitude, weather, iceInformation);
        
        this.speed = speed;
        this.course = course;
    }

    public GreenPosPositionReport(String shipName, Long shipMmsi, String shipCallSign, String shipMaritimeId,
            Position position, String weather, String iceInformation, Double speed, Integer course) {
        super(shipName, shipMmsi, shipCallSign, shipMaritimeId, position, weather, iceInformation);
        
        this.speed = speed;
        this.course = course;
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
    public Double getSpeed() {
        return speed;
    }

    public Integer getCourse() {
        return course;
    }

}
