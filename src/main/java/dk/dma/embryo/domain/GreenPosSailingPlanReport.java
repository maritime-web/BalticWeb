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

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;

import dk.dma.embryo.rest.json.GreenPos;
import dk.dma.embryo.rest.util.DateTimeConverter;

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

    @NotNull
    private LocalDateTime etaOfArrival;

    @Valid
    @OneToMany(cascade = CascadeType.ALL)
    @OrderColumn(name = "orderNumber")
    private List<ReportedVoyage> voyages = new ArrayList<>();

    // //////////////////////////////////////////////////////////////////////
    // business logic
    // //////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////

    public static GreenPosSailingPlanReport fromJsonModel(GreenPos from) {
        LocalDateTime eta = DateTimeConverter.getDateTimeConverter().toObject(from.getEtaOfArrival(), null);
        Position pos = new Position(from.getLat(), from.getLon());

        GreenPosSailingPlanReport report = new GreenPosSailingPlanReport(from.getShipName(), from.getMmsi(),
                from.getCallSign(), from.getShipMaritimeId(), pos, from.getWeather(), from.getIce(),
                from.getSpeed(), from.getCourse(), from.getDestination(), eta, from.getPersonsOnBoard());
        return report;
    }

    @Override
    public GreenPos toJsonModel() {
        String eta = DateTimeConverter.getDateTimeConverter().toString(getEtaOfArrival(), null);
        
        List<dk.dma.embryo.rest.json.Voyage> transformed = new ArrayList<dk.dma.embryo.rest.json.Voyage>(getVoyages().size());
        for(ReportedVoyage v : getVoyages()){
            transformed.add(v.toJsonModel());
        }
        
        GreenPos result = new GreenPos();
        result.setType(getReportType());
        result.setShipName(getShipName());
        result.setShipMaritimeId(getShipMaritimeId());
        result.setMmsi(getShipMmsi());
        result.setCallSign(getShipCallSign());
        result.setLon(getPosition().getLongitudeAsString());
        result.setLat(getPosition().getLatitudeAsString());
        result.setWeather(getWeather());
        result.setIce(getIceInformation());
        result.setSpeed(getSpeed());
        result.setCourse(getCourse());
        result.setDestination(getDestination());
        result.setPersonsOnBoard(getPersonsOnBoard());
        result.setEtaOfArrival(eta);
        result.setVoyages(transformed);
        result.setReporter(getReportedBy());
        result.setTs(getTs().toDateTime(DateTimeZone.UTC).getMillis());
        return result;
    }

    public GreenPosSailingPlanReport withVoyages(List<Voyage> voyages) {
        List<ReportedVoyage> transformed = new ArrayList<>(voyages.size());

        for (Voyage voyage : voyages) {
            transformed.add(ReportedVoyage.from(voyage));
        }

        return new GreenPosSailingPlanReport(getShipName(), getShipMmsi(), getShipCallSign(), getShipMaritimeId(),
                getPosition(), getWeather(), getIceInformation(), getSpeed(), getCourse(), getDestination(),
                getEtaOfArrival(), getPersonsOnBoard(), transformed);
    }

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public GreenPosSailingPlanReport() {
    }

    public GreenPosSailingPlanReport(String shipName, Long shipMmsi, String shipCallSign, String shipMaritimeId,
            Position position, String weather, String iceInformation, Double speed, Integer course, String destination,
            LocalDateTime eta, Integer personsOnBoard) {
        super(shipName, shipMmsi, shipCallSign, shipMaritimeId, position, weather, iceInformation, speed, course);

        this.destination = destination;
        this.personsOnBoard = personsOnBoard;
        this.etaOfArrival = eta;
    }

    public GreenPosSailingPlanReport(String shipName, Long shipMmsi, String shipCallSign, String shipMaritimeId,
            Position position, String weather, String iceInformation, Double speed, Integer course, String destination,
            LocalDateTime eta, Integer personsOnBoard, List<ReportedVoyage> voyages) {
        this(shipName, shipMmsi, shipCallSign, shipMaritimeId, position, weather, iceInformation, speed, course,
                destination, eta, personsOnBoard);

        this.voyages = voyages;
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

    public LocalDateTime getEtaOfArrival() {
        return etaOfArrival;
    }

    public Integer getPersonsOnBoard() {
        return personsOnBoard;
    }

    public List<ReportedVoyage> getVoyages() {
        return voyages;
    }
    
    

}
