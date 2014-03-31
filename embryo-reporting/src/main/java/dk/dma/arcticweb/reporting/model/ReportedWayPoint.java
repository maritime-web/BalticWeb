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
package dk.dma.arcticweb.reporting.model;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import dk.dma.embryo.vessel.model.Position;
import dk.dma.embryo.vessel.model.WayPoint;

@Embeddable
public class ReportedWayPoint implements Serializable {

    private static final long serialVersionUID = -7205030526506222850L;

    // //////////////////////////////////////////////////////////////////////
    // Entity fields (also see super class)
    // //////////////////////////////////////////////////////////////////////

    @NotNull
    @Size(min = 1)
    private String name;

    @Valid
    private Position position;

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////

    public static ReportedWayPoint fromModel(WayPoint wayPoint) {
        ReportedWayPoint transformed = new ReportedWayPoint(wayPoint.getName(), wayPoint.getPosition().getLatitude(),
                wayPoint.getPosition().getLongitude());
        return transformed;
    }

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public ReportedWayPoint() {
        position = new Position();
    }

    public ReportedWayPoint(String name, Double latitude, Double longitude) {
        this.name = name;
        position = new Position(latitude, longitude);
    }

    public ReportedWayPoint(String name, String latitude, String longitude) {
        this.name = name;
        position = new Position(latitude, longitude);
    }

    
    // //////////////////////////////////////////////////////////////////////
    // Object methods
    // //////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
        return "ReportedWayPoint [name=" + name + ", position=" + position + "]";
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

    public void setName(String name) {
        this.name = name;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

}
