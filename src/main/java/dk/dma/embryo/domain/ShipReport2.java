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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import dk.dma.arcticweb.domain.BaseEntity;

@Entity
public class ShipReport2 extends BaseEntity<Long> {

    private static final long serialVersionUID = 1L;

    @Column(unique = false, nullable = true)
    private Double lat;

    @Column(unique = false, nullable = true)
    private Double lon;

    @Column(unique = false, nullable = true)
    private String weather;

    @Column(unique = false, nullable = true)
    private String iceObservations;

    @Column(unique = false, nullable = false)
    private Date reportTime;

    @Column(unique = false, nullable = false, updatable = false)
    private Date created;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Ship2 ship;

    public ShipReport2() {
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getIceObservations() {
        return iceObservations;
    }

    public void setIceObservations(String iceObservations) {
        this.iceObservations = iceObservations;
    }

    public Date getReportTime() {
        return reportTime;
    }

    public void setReportTime(Date reportTime) {
        this.reportTime = reportTime;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Ship2 getShip() {
        return ship;
    }

    public void setShip(Ship2 ship) {
        this.ship = ship;
    }

}
