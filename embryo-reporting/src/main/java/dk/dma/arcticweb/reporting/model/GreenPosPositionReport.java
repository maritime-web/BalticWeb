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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

/**
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
        Position pos = new Position(from.getLat(), from.getLon());
        GreenPosPositionReport report = new GreenPosPositionReport(from.getVesselName(), from.getMmsi(),
                from.getCallSign(), pos, from.getNumber(), from.getWeather(), from.getIce(),
                from.getSpeed(), from.getCourse(), from.getMalFunctions());

        return report;
    }

    @Override
    public GreenPos toJsonModel() {
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
        result.setIce(getIceInformation());
        result.setSpeed(getSpeed());
        result.setCourse(getCourse());
        result.setReporter(getReportedBy());
        result.setTs(getTs().toDate());
        result.setRecipient(getRecipient());

        return result;
    }

    @Override
    public GreenPosShort toJsonModelShort() {
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
        result.setTs(getTs().toDate());
        result.setRecipient(getRecipient());
        result.setMalFunctions(getVesselMalFunctions());

        return result;
    }


    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public GreenPosPositionReport() {
        super();
    }

    public GreenPosPositionReport(String vesselName, Long vesselMmsi, String vesselCallSign,
                                  String latitude, String longitude, Integer number, String weather, String iceInformation, Double speed, Integer course, String vesselMalFunctions) {
        super(vesselName, vesselMmsi, vesselCallSign, latitude, longitude, number, weather, iceInformation, vesselMalFunctions);

        this.speed = speed;
        this.course = course;
    }

    public GreenPosPositionReport(String vesselName, Long vesselMmsi, String vesselCallSign,
                                  Position position, Integer number, String weather, String iceInformation, Double speed, Integer course, String vesselMalFunctions) {
        super(vesselName, vesselMmsi, vesselCallSign, position, number, weather, iceInformation, vesselMalFunctions);
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
