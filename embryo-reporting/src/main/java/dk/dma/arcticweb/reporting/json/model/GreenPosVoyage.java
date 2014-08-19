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
package dk.dma.arcticweb.reporting.json.model;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import dk.dma.enav.model.geometry.Position;

public class GreenPosVoyage implements Serializable{

    private static final long serialVersionUID = -7205030526506222850L;

    // //////////////////////////////////////////////////////////////////////
    // Entity fields (also see super class)
    // //////////////////////////////////////////////////////////////////////
    private String maritimeId;

    private String location;

    private Position position;

    private Date arrival;

    private Date departure;

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

    public GreenPosVoyage(String maritimeId, String location, Position position, Date arrival,
            Date departure, Integer personsOnBoard, boolean doctorOnBoard) {
        this();
        this.location = location;
        this.position = position;
        this.arrival = arrival;
        this.departure = departure;
        this.personsOnBoard = personsOnBoard;
        this.doctorOnBoard = doctorOnBoard;
    }

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public Date getArrival() {
        return arrival;
    }

    public Date getDeparture() {
        return departure;
    }

    public String getLocation() {
        return location;
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
