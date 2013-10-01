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

import java.util.List;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public class GreenPos {

    private String id;

    private String type;

    private String shipName;

    private Long mmsi;

    private String callSign;

    private String shipMaritimeId;

    private String lat;

    private String lon;

    private String weather;

    private String ice;

    private Double speed;

    private Integer course;

    private String destination;

    private Integer personsOnBoard;

    private String etaOfArrival;

    private String deviation;

    private List<Voyage> voyages;

    private String reporter;

    private Long ts;

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public GreenPos() {
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
    public String getShipName() {
        return shipName;
    }

    public void setShipName(String shipName) {
        this.shipName = shipName;
    }

    public Long getMmsi() {
        return mmsi;
    }

    public void setMmsi(Long shipMmsi) {
        this.mmsi = shipMmsi;
    }

    public String getCallSign() {
        return callSign;
    }

    public void setCallSign(String shipCallSign) {
        this.callSign = shipCallSign;
    }

    public String getShipMaritimeId() {
        return shipMaritimeId;
    }

    public void setShipMaritimeId(String shipMaritimeId) {
        this.shipMaritimeId = shipMaritimeId;
    }

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

    public String getReporter() {
        return reporter;
    }

    public void setReporter(String reportedBy) {
        this.reporter = reportedBy;
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

    public String getEtaOfArrival() {
        return etaOfArrival;
    }

    public void setEtaOfArrival(String etaOfArrival) {
        this.etaOfArrival = etaOfArrival;
    }

    public String getDeviation() {
        return deviation;
    }

    public void setDeviation(String deviation) {
        this.deviation = deviation;
    }

    public List<Voyage> getVoyages() {
        return voyages;
    }

    public void setVoyages(List<Voyage> voyages) {
        this.voyages = voyages;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long reportedTs) {
        this.ts = reportedTs;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
