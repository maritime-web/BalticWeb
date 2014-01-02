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

import java.util.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import dk.dma.embryo.rest.json.GreenPos;
import dk.dma.embryo.rest.json.GreenPosShort;

/**
 * 
 * @author Jesper Tejlgaard
 */
@Entity
@DiscriminatorValue("SP")
public class GreenPosSailingPlanReport extends GreenPosPositionReport {

    private static final long serialVersionUID = -7205030526506222850L;

    // //////////////////////////////////////////////////////////////////////
    // Entity fields (also see super class)
    // //////////////////////////////////////////////////////////////////////
    @NotNull
    private String destination;

    @NotNull
    private Integer personsOnBoard;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @NotNull
    private DateTime etaOfArrival;

    // //////////////////////////////////////////////////////////////////////
    // business logic
    // //////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////

    public static GreenPosSailingPlanReport fromJsonModel(GreenPos from) {
        DateTime eta = from.getEta() == null ? null : new DateTime(from.getEta().getTime(), DateTimeZone.UTC); 
        Position pos = new Position(from.getLat(), from.getLon());

        GreenPosSailingPlanReport report = new GreenPosSailingPlanReport(from.getVesselName(), from.getMmsi(),
                from.getCallSign(), pos, from.getWeather(), from.getIce(),
                from.getSpeed(), from.getCourse(), from.getDestination(), eta, from.getPersonsOnBoard());
        return report;
    }

    @Override
    public GreenPos toJsonModel() {
        Date eta = getEtaOfArrival() == null ? null : getEtaOfArrival().toDate(); 
        
//        List<dk.dma.embryo.rest.json.Voyage> transformed = new ArrayList<dk.dma.embryo.rest.json.Voyage>(getVoyages().size());
//        for(ReportedVoyage v : getVoyages()){
//            transformed.add(v.toJsonModel());
//        }
        
        GreenPos result = new GreenPos();
        result.setId(getEnavId());
        result.setType(getReportType());
        result.setVesselName(getVesselName());
        result.setMmsi(getVesselMmsi());
        result.setCallSign(getVesselCallSign());
        result.setLon(getPosition().getLongitude());
        result.setLat(getPosition().getLatitude());
        result.setWeather(getWeather());
        result.setIce(getIceInformation());
        result.setSpeed(getSpeed());
        result.setCourse(getCourse());
        result.setDestination(getDestination());
        result.setPersonsOnBoard(getPersonsOnBoard());
        result.setEta(eta);
//        result.setVoyages(transformed);
        result.setReporter(getReportedBy());
        result.setTs(getTs().toDate());
        return result;
    }

    @Override
    public GreenPosShort toJsonModelShort() {
        Date eta = getEtaOfArrival() == null ? null : getEtaOfArrival().toDate();
        
        GreenPosShort result = new GreenPosShort();
        result.setId(getEnavId());
        result.setType(getReportType());
        result.setLon(getPosition().getLongitudeAsString());
        result.setLat(getPosition().getLatitudeAsString());
        result.setWeather(getWeather());
        result.setIce(getIceInformation());
        result.setSpeed(getSpeed());
        result.setCourse(getCourse());
        result.setDestination(getDestination());
        result.setPersonsOnBoard(getPersonsOnBoard());
        result.setEta(eta);
        result.setTs(getTs().toDate());
        return result;
    }

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public GreenPosSailingPlanReport() {
    }

    public GreenPosSailingPlanReport(String vesselName, Long vesselMmsi, String vesselCallSign,
            Position position, String weather, String iceInformation, Double speed, Integer course, String destination,
            DateTime eta, Integer personsOnBoard) {
        super(vesselName, vesselMmsi, vesselCallSign, position, weather, iceInformation, speed, course);

        this.destination = destination;
        this.personsOnBoard = personsOnBoard;
        this.etaOfArrival = eta;
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
    public String getDestination() {
        return destination;
    }

    public DateTime getEtaOfArrival() {
        return etaOfArrival;
    }

    public Integer getPersonsOnBoard() {
        return personsOnBoard;
    }
}
