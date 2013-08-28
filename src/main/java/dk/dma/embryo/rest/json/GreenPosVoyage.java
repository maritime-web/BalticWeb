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

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.joda.time.LocalDateTime;

import dk.dma.enav.model.geometry.Position;

public class GreenPosVoyage implements Serializable{

    private static final long serialVersionUID = -7205030526506222850L;

    // //////////////////////////////////////////////////////////////////////
    // Entity fields (also see super class)
    // //////////////////////////////////////////////////////////////////////
    private String maritimeId;

    private String berthName;

    private Position position;

    private LocalDateTime arrival;

    private LocalDateTime departure;

    private Integer personsOnBoard;

    private boolean doctorOnBoard;
    
    // //////////////////////////////////////////////////////////////////////
    // business logic
    // //////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public GreenPosVoyage() {
    }

    public GreenPosVoyage(String maritimeId, String name, Position position, LocalDateTime arrival,
            LocalDateTime departure, Integer personsOnBoard, boolean doctorOnBoard) {
        this();
        this.berthName = name;
        this.position = position;
        this.arrival = arrival;
        this.departure = departure;
        this.personsOnBoard = personsOnBoard;
        this.doctorOnBoard = doctorOnBoard;
    }

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public LocalDateTime getArrival() {
        return arrival;
    }

    public LocalDateTime getDeparture() {
        return departure;
    }

    public String getBerthName() {
        return berthName;
    }

    public Position getPosition() {
        return position;
    }

    public String getMaritimeId() {
        return maritimeId;
    }

    public Integer getPersonsOnBoard() {
        return personsOnBoard;
    }

    public boolean getDoctorOnBoard() {
        return doctorOnBoard;
    }
}
