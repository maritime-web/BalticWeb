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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.joda.time.DateTimeZone;

import dk.dma.embryo.rest.json.GreenPos;

/**
 * 
 * @author Jesper Tejlgaard
 */
@Entity
@DiscriminatorValue("FR")
public class GreenPosFinalReport extends GreenPosDMIReport {

    private static final long serialVersionUID = -7205030526506222850L;

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////
    public static GreenPosFinalReport fromJsonModel(GreenPos from) {
        Position pos = new Position(from.getLatitude(), from.getLongitude());

        GreenPosFinalReport report = new GreenPosFinalReport(from.getShipName(), from.getShipMmsi(),
                from.getShipCallSign(), from.getShipMaritimeId(), pos, from.getWeather(), from.getIce());

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
        result.setReportedBy(getReportedBy());
        result.setReportedTs(getTs().toDateTime(DateTimeZone.UTC).getMillis());
        
        return result;
    }


    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public GreenPosFinalReport() {
        super();
    }

    public GreenPosFinalReport(String shipName, Long shipMmsi, String shipCallSign, String shipMaritimeId,
            Position position, String weather, String iceInformation) {
        super(shipName, shipMmsi, shipCallSign, shipMaritimeId, position, weather, iceInformation);
    }

    // //////////////////////////////////////////////////////////////////////
    // Object methods
    // //////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
