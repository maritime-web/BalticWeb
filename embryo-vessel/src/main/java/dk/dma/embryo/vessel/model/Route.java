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
package dk.dma.embryo.vessel.model;

import dk.dma.embryo.common.persistence.BaseEntity;
import dk.dma.enav.model.voyage.Waypoint;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@NamedQueries({ @NamedQuery(name = "Route:getByEnavId", query = "SELECT DISTINCT r FROM Route r LEFT JOIN FETCH r.wayPoints where r.enavId = :enavId"),
        @NamedQuery(name = "Route:getId", query = "SELECT r.id FROM Route r WHERE r.enavId = :enavId"),
        @NamedQuery(name = "Route:mmsi", query = "SELECT ves.mmsi FROM Route r INNER JOIN r.voyage v INNER JOIN v.vessel ves WHERE r.enavId = :enavId") })
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

    @ElementCollection()
    @CollectionTable(name = "WayPoint")
    @OrderColumn(name = "orderNumber")
    @Valid
    private List<WayPoint> wayPoints = new ArrayList<>();

    @OneToOne(mappedBy = "route")
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

    public static Route fromJsonModel(dk.dma.embryo.vessel.json.Route from) {

        Route route = new Route(from.getId(), from.getName(), from.getDep(), from.getDes());

        for (dk.dma.embryo.vessel.json.Waypoint wayPoint : from.getWps()) {
            route.addWayPoint(WayPoint.fromJsonModel(wayPoint));
        }

        return route;
    }

    public dk.dma.embryo.vessel.json.Route toJsonModel() {
        dk.dma.embryo.vessel.json.Route toRoute = new dk.dma.embryo.vessel.json.Route(this.enavId);
        toRoute.setName(this.name);
        toRoute.setDep(this.origin);
        toRoute.setDes(this.destination);
        toRoute.setEtaDep(this.getVoyage().getDeparture().toDate());
        toRoute.setEta(this.getVoyage().getArrival().toDate());
        for (WayPoint wp : this.getWayPoints()) {
            toRoute.getWps().add(wp.toJsonModel());
        }

        return toRoute;
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
        return "Route [name=" + name + ", destination=" + destination + ", origin=" + origin + ", wayPoints" + wayPoints + "]";
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

    public List<WayPoint> getWayPoints() {
        return wayPoints;
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
