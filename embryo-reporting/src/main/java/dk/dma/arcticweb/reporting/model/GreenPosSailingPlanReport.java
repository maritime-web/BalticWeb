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

import dk.dma.arcticweb.reporting.json.model.GreenPos;
import dk.dma.arcticweb.reporting.json.model.GreenPosShort;
import dk.dma.embryo.vessel.model.Position;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
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

    @Size(max = 1000)
    private String routeDescription;

    @OneToOne(cascade = CascadeType.PERSIST)
    private ReportedRoute route;


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
                from.getCallSign(), pos, from.getNumber(), from.getWeather(), from.getIce(), from.getSpeed(), from.getCourse(),
                from.getDestination(), eta, from.getPersonsOnBoard(), from.getDescription(), from.getMalFunctions());
        return report;
    }

    @Override
    public GreenPos toJsonModel() {
        Date eta = getEtaOfArrival() == null ? null : getEtaOfArrival().toDate();

        GreenPos result = new GreenPos();
        result.setId(getEnavId());
        result.setType(getReportType());
        result.setVesselName(getVesselName());
        result.setMmsi(getVesselMmsi());
        result.setCallSign(getVesselCallSign());
        result.setLon(getPosition().getLongitude());
        result.setLat(getPosition().getLatitude());
        result.setNumber(getNumber());
        result.setWeather(getWeather());
        result.setMalFunctions(getVesselMalFunctions());
        result.setIce(getIceInformation());
        result.setSpeed(getSpeed());
        result.setCourse(getCourse());
        result.setDestination(getDestination());
        result.setPersonsOnBoard(getPersonsOnBoard());
        result.setEta(eta);
        result.setReporter(getReportedBy());
        result.setTs(getTs().toDate());
        result.setRecipient(getRecipient());
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
        result.setNumber(getNumber());
        result.setWeather(getWeather());
        result.setIce(getIceInformation());
        result.setSpeed(getSpeed());
        result.setCourse(getCourse());
        result.setDestination(getDestination());
        result.setPersonsOnBoard(getPersonsOnBoard());
        result.setEta(eta);
        result.setTs(getTs().toDate());
        result.setRecipient(getRecipient());
        result.setMalFunctions(getVesselMalFunctions());

        return result;
    }

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public GreenPosSailingPlanReport() {
    }

    public GreenPosSailingPlanReport(String vesselName, Long vesselMmsi, String vesselCallSign, Position position,
                                     Integer number, String weather, String iceInformation, Double speed, Integer course, String destination, DateTime eta,
                                     Integer personsOnBoard, String routeDescription, String vesselMalFunctions) {
        super(vesselName, vesselMmsi, vesselCallSign, position, number, weather, iceInformation, speed, course, vesselMalFunctions);

        this.destination = destination;
        this.personsOnBoard = personsOnBoard;
        this.etaOfArrival = eta;
        this.routeDescription = routeDescription;
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

    public String getRouteDescription() {
        return routeDescription;
    }

    public ReportedRoute getRoute() {
        return route;
    }

    public void setRoute(ReportedRoute route) {
        this.route = route;
    }
}
