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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import dk.dma.embryo.rest.json.GreenPos;
import dk.dma.embryo.rest.util.DateTimeConverter;

/**
 * 
 * @author Jesper Tejlgaard
 */
@Entity
@DiscriminatorValue("PR")
public class GreenPosPositionReport extends GreenPosDMIReport {

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
    public static GreenPosPositionReport fromJsonModel(GreenPos from) {
        Position pos = new Position(from.getLatitude(), from.getLongitude());

        GreenPosPositionReport report = new GreenPosPositionReport(from.getShipName(), from.getShipMmsi(),
                from.getShipCallSign(), from.getShipMaritimeId(), pos, from.getWeather(), from.getIce(),
                from.getSpeed(), from.getCourse());

        return report;
    }

    @Override
    public GreenPos toJsonModel() {
        GreenPos result = new GreenPos();
        result.setReportType(getReportType());
        result.setShipName(getShipName());
        result.setShipMaritimeId(getShipMaritimeId());
        result.setShipMmsi(getShipMmsi());
        result.setShipCallSign(getShipCallSign());
        result.setLongitude(getPosition().getLongitudeAsString());
        result.setLatitude(getPosition().getLatitudeAsString());
        result.setWeather(getWeather());
        result.setIce(getIceInformation());
        result.setSpeed(getSpeed());
        result.setCourse(getCourse());
        result.setReportedBy(getReportedBy());
        
        return result;
    }

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
