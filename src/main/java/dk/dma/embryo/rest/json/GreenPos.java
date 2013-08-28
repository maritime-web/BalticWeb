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

import dk.dma.enav.model.voyage.Voyage;


public class GreenPos {
    
    private String shipName;
    
    private Long shipMmsi;

    private String shipCallSign;

    private String shipMaritimeId;

    private String latitude;

    private String longitude;

    private String reportType;
    
    private String weather;
    
    private String iceInformation;
    
    private String reportedBy;

    private Double speed;
    
    private Integer course;
    
    private String destination;
    
    private Integer personsOnBoard;
    
    private String etaOfArrival;
    
    private String deviation;

    private List<Voyage> voyages;

    
    
    public String getShipName() {
        return shipName;
    }

    public void setShipName(String shipName) {
        this.shipName = shipName;
    }

    public Long getShipMmsi() {
        return shipMmsi;
    }

    public void setShipMmsi(Long shipMmsi) {
        this.shipMmsi = shipMmsi;
    }

    public String getShipCallSign() {
        return shipCallSign;
    }

    public void setShipCallSign(String shipCallSign) {
        this.shipCallSign = shipCallSign;
    }

    public String getShipMaritimeId() {
        return shipMaritimeId;
    }

    public void setShipMaritimeId(String shipMaritimeId) {
        this.shipMaritimeId = shipMaritimeId;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getIceInformation() {
        return iceInformation;
    }

    public void setIceInformation(String iceInformation) {
        this.iceInformation = iceInformation;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(String reportedBy) {
        this.reportedBy = reportedBy;
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
   

}
