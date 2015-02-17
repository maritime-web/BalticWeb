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

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.util.Date;

public class GreenPosShort {

    private String id;
    private String type;
    private String lat;
    private String lon;
    private Integer number;
    private String weather;
    private String ice;
    private Double speed;
    private Integer course;
    private String destination;
    private Integer personsOnBoard;
    private Date eta;
    private String deviation;
    private Date ts;
    private String recipient;
    private String malFunctions;

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public GreenPosShort() {
    }

    // //////////////////////////////////////////////////////////////////////
    // Object methods
    // //////////////////////////////////////////////////////////////////////
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((course == null) ? 0 : course.hashCode());
        result = prime * result + ((destination == null) ? 0 : destination.hashCode());
        result = prime * result + ((deviation == null) ? 0 : deviation.hashCode());
        result = prime * result + ((eta == null) ? 0 : eta.hashCode());
        result = prime * result + ((ice == null) ? 0 : ice.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((lat == null) ? 0 : lat.hashCode());
        result = prime * result + ((lon == null) ? 0 : lon.hashCode());
        result = prime * result + ((malFunctions == null) ? 0 : malFunctions.hashCode());
        result = prime * result + ((number == null) ? 0 : number.hashCode());
        result = prime * result + ((personsOnBoard == null) ? 0 : personsOnBoard.hashCode());
        result = prime * result + ((recipient == null) ? 0 : recipient.hashCode());
        result = prime * result + ((speed == null) ? 0 : speed.hashCode());
        result = prime * result + ((ts == null) ? 0 : ts.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((weather == null) ? 0 : weather.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public String getLat() {
        return lat;
    }
    public void setLat(String latitude) {
        this.lat = latitude;
    }

    public String getLon() {
        return lon;
    }
    public void setLon(String longitude) {
        this.lon = longitude;
    }

    public Integer getNumber() {
        return number;
    }
    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }
    public void setType(String reportType) {
        this.type = reportType;
    }

    public String getWeather() {
        return weather;
    }
    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getIce() {
        return ice;
    }
    public void setIce(String iceInformation) {
        this.ice = iceInformation;
    }

    public Double getSpeed() {
        return speed;
    }
    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Integer getCourse() {
        return course;
    }
    public void setCourse(Integer course) {
        this.course = course;
    }

    public String getDestination() {
        return destination;
    }
    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Integer getPersonsOnBoard() {
        return personsOnBoard;
    }
    public void setPersonsOnBoard(Integer personsOnBoard) {
        this.personsOnBoard = personsOnBoard;
    }

    public Date getEta() {
        return eta;
    }
    public void setEta(Date eta) {
        this.eta = eta;
    }

    public String getDeviation() {
        return deviation;
    }
    public void setDeviation(String deviation) {
        this.deviation = deviation;
    }

    public Date getTs() {
        return ts;
    }
    public void setTs(Date reportedTs) {
        this.ts = reportedTs;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getRecipient() {
        return recipient;
    }
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getMalFunctions() {
        return malFunctions;
    }
    public void setMalFunctions(String malFunctions) {
        this.malFunctions = malFunctions;
    }
}
