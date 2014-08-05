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
