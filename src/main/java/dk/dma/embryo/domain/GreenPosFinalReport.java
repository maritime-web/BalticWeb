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

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * 
 * @author Jesper Tejlgaard
 */
@Entity
public class GreenPosFinalReport extends GreenPosReport {

    private static final long serialVersionUID = -7205030526506222850L;

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
    public GreenPosFinalReport() {
        super();
    }

    // //////////////////////////////////////////////////////////////////////
    // Object methods
    // //////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
