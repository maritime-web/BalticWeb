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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

@Entity
@NamedQueries({
        @NamedQuery(name = "Voyage:getByEnavId", query = "SELECT DISTINCT v FROM Voyage v where v.enavId = :enavId"),
        @NamedQuery(name = "Voyage:getByEnavIds", query = "SELECT DISTINCT v FROM Voyage v where v.enavId in :enavIds"),
        @NamedQuery(name = "Voyage:getByMmsi", query = "SELECT DISTINCT v FROM Voyage v where v.vessel.mmsi = :mmsi AND (:date IS NULL OR DATE(v.departure) >= DATE(:date)) order by v.departure") })
public class Voyage extends BaseEntity<Long> {

    private static final long serialVersionUID = -7205030526506222850L;

    // //////////////////////////////////////////////////////////////////////
    // Entity fields (also see super class)
    // //////////////////////////////////////////////////////////////////////
    private String enavId;

    @NotNull
    private String berthName;

    @Valid
    private Position position;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime arrival;

    @NotNull
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime departure;

    private Integer passengersOnBoard;
    private Integer crewOnBoard;
    private Boolean doctorOnBoard;

    // Should cascade be set to e.g. MERGE, REMOVE, PERSIST?
    @OneToOne
    Route route;

    @ManyToOne
    Vessel vessel;

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////
    public dk.dma.embryo.rest.json.Voyage toJsonModel() {
        Date arrival = getArrival() == null ? null : getArrival().toDate();
        Date departure = getDeparture() == null ? null : getDeparture().toDate();

        dk.dma.embryo.rest.json.Voyage voyage = new dk.dma.embryo.rest.json.Voyage(getEnavId(), getBerthName(),
                getPosition().getLatitude(), getPosition().getLongitude(), arrival, departure,
                getCrewOnBoard(), getPassengersOnBoard(), getDoctorOnBoard());

        return voyage;
    }

    public static List<Voyage> fromJsonModel(dk.dma.embryo.rest.json.Voyage[] voyages) {
        List<Voyage> result = new ArrayList<>(voyages.length);
        for (dk.dma.embryo.rest.json.Voyage voyage : voyages) {
            Voyage v = Voyage.fromJsonModel(voyage);
            result.add(v);
        }
        return result;
    }

    public static Voyage fromJsonModel(dk.dma.embryo.rest.json.Voyage voyage) {
        DateTime arrival = voyage.getArrival() == null ? null : new DateTime(voyage.getArrival().getTime(), DateTimeZone.UTC);
        DateTime departure = voyage.getDeparture() == null ? null : new DateTime(voyage.getDeparture().getTime(), DateTimeZone.UTC);
        
        Position position = new Position(voyage.getLatitude(), voyage.getLongitude());

        Voyage result = new Voyage(voyage.getMaritimeId());
        result.setBerthName(voyage.getBerthName());
        result.setPosition(position);
        result.setArrival(arrival);
        result.setDeparture(departure);
        result.setCrewOnBoard(voyage.getCrew());
        result.setPassengersOnBoard(voyage.getPassengers());
        result.setDoctorOnBoard(voyage.isDoctor());
        return result;
    }

    public static Map<String, Voyage> asMap(List<Voyage> voyages) {
        Map<String, Voyage> m = new HashMap<>();
        for (Voyage v : voyages) {
            m.put(v.getEnavId(), v);
        }
        return m;
    }

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public Voyage(String key) {
        if (key == null || key.trim().length() == 0) {
            key = UUID.randomUUID().toString();
        }
        this.enavId = key;
        position = new Position();
    }

    public Voyage() {
        this(null);
    }

    public Voyage(String name, String latitude, String longitude, DateTime arrival, DateTime departure) {
        this();
        this.berthName = name;
        this.position = new Position(latitude, longitude);
        this.arrival = arrival;
        this.departure = departure;
    }

    public Voyage(String name, String latitude, String longitude, DateTime arrival, DateTime departure,
            Integer crewOnBoard, Integer passengers, boolean doctorOnBoard) {
        this();
        this.berthName = name;
        this.position = new Position(latitude, longitude);
        this.arrival = arrival;
        this.departure = departure;
        this.crewOnBoard = crewOnBoard;
        this.passengersOnBoard = passengers;
        this.doctorOnBoard = doctorOnBoard;
    }

    // //////////////////////////////////////////////////////////////////////
    // Object methods
    // //////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
        return "Voyage [" + baseToString() + ", enavId=" + enavId + ", berthName=" + berthName + ", position="
                + position + ", arrival=" + arrival + ", departure=" + departure + ", crewOnBoard=" + crewOnBoard
                + ", passengersOnBoard=" + passengersOnBoard + ", doctorOnBoard=" + doctorOnBoard + "]";
    }

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public DateTime getArrival() {
        return arrival;
    }

    public void setArrival(DateTime arrival) {
        this.arrival = arrival;
    }

    public DateTime getDeparture() {
        return departure;
    }

    public void setDeparture(DateTime departure) {
        this.departure = departure;
    }

    public String getBerthName() {
        return berthName;
    }

    public void setBerthName(String berthName) {
        this.berthName = berthName;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public String getEnavId() {
        return enavId;
    }

    public void setEnavId(String key) {
        // hack such that wicket does not overwrite generated key with empty value
        if (key == null || key.length() == 0) {
            return;
        }
        this.enavId = key;
    }

    public Integer getCrewOnBoard() {
        return crewOnBoard;
    }

    public void setCrewOnBoard(Integer crewOnBoard) {
        this.crewOnBoard = crewOnBoard;
    }

    public Integer getPassengersOnBoard() {
        return passengersOnBoard;
    }

    public void setPassengersOnBoard(Integer passengersOnBoard) {
        this.passengersOnBoard = passengersOnBoard;
    }

    public Boolean getDoctorOnBoard() {
        return doctorOnBoard;
    }

    public void setDoctorOnBoard(Boolean doctorOnBoard) {
        this.doctorOnBoard = doctorOnBoard;
    }

    public Route getRoute() {
        return route;
    }

    public Vessel getVessel() {
        return vessel;
    }

}
