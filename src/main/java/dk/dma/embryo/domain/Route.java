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
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.joda.time.LocalDateTime;

import dk.dma.enav.model.voyage.Waypoint;

@Entity
@NamedQueries({
        @NamedQuery(name = "Route:getByEnavId", query = "SELECT DISTINCT r FROM Route r LEFT JOIN FETCH r.wayPoints where r.enavId = :enavId"),
        @NamedQuery(name = "Route:getId", query = "SELECT r.id FROM Route r WHERE r.enavId = :enavId") })
public class Route extends BaseEntity<Long> {

    private static final long serialVersionUID = -7205030526506222850L;

    // //////////////////////////////////////////////////////////////////////
    // Entity fields (also see super class)
    // //////////////////////////////////////////////////////////////////////
    @NotNull
    private String name;

    @NotNull
    private String enavId;

    private String destination;

    private String origin;

    private LocalDateTime etaOfArrival;

    private LocalDateTime etaOfDeparture;

    @ElementCollection
    @CollectionTable(name = "WayPoint")
    @OrderColumn(name = "orderNumber")
    @Valid
    private List<WayPoint> wayPoints = new ArrayList<>();

    @OneToOne
    private Ship2 ship;

    @OneToOne(mappedBy="route", cascade={CascadeType.PERSIST, CascadeType.MERGE})
    private Voyage voyage;

    // //////////////////////////////////////////////////////////////////////
    // business logic
    // //////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////
    public void addWayPoint(WayPoint wPoint) {
        wayPoints.add(wPoint);
    }

    public static Route fromEnavModel(dk.dma.enav.model.voyage.Route from) {
        Route route = new Route(from.getId(), from.getName(), from.getDeparture(), from.getDestination());

        for (Waypoint wayPoint : from.getWaypoints()) {
            route.addWayPoint(WayPoint.fromEnavModel(wayPoint));
        }

        return route;
    }

    public dk.dma.enav.model.voyage.Route toEnavModel() {
        dk.dma.enav.model.voyage.Route toRoute = new dk.dma.enav.model.voyage.Route(this.enavId);
        toRoute.setName(this.name);
        toRoute.setDeparture(this.origin);
        toRoute.setDestination(this.destination);

        for (WayPoint wp : this.getWayPoints()) {
            toRoute.getWaypoints().add(wp.toEnavModel());
        }

        return toRoute;
    }

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public Route() {
        this.enavId = UUID.randomUUID().toString();
    }

    public Route(String key, String name, String origin, String destination) {
        super();
        this.enavId = key != null ? key : UUID.randomUUID().toString();
        this.name = name;
        this.destination = destination;
        this.origin = origin;
    }

    // //////////////////////////////////////////////////////////////////////
    // Object methods
    // //////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
        return "Route [name=" + name + ", destination=" + destination + ", origin=" + origin + ", etaOfArrival="
                + etaOfArrival + ", etaOfDeparture=" + etaOfDeparture + ", wayPoints" + wayPoints + "]";
    }

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public LocalDateTime getEtaOfArrival() {
        return etaOfArrival;
    }

    public void setEtaOfArrival(LocalDateTime etaOfArrival) {
        this.etaOfArrival = etaOfArrival;
    }

    public LocalDateTime getEtaOfDeparture() {
        return etaOfDeparture;
    }

    public void setEtaOfDeparture(LocalDateTime etaOfDeparture) {
        this.etaOfDeparture = etaOfDeparture;
    }

    public List<WayPoint> getWayPoints() {
        return wayPoints;
    }

    public Ship2 getShip() {
        return ship;
    }

    public void setShip(Ship2 ship) {
        this.ship = ship;
    }

    public Voyage getVoyage() {
        return voyage;
    }

    public void setVoyage(Voyage voyage) {
        this.voyage = voyage;
        this.voyage.route = this;
    }

    public String getEnavId() {
        return enavId;
    }

    public void setEnavId(String enavId) {
        this.enavId = enavId;
    }

    public void setId(Long id) {
        if (this.id != null) {
            throw new IllegalStateException("Can not modify existing id");
        }

        this.id = id;
    }
}
