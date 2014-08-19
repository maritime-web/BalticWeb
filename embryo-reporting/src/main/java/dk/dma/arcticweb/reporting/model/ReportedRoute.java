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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.OrderColumn;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import dk.dma.embryo.common.persistence.BaseEntity;
import dk.dma.embryo.vessel.model.Route;
import dk.dma.embryo.vessel.model.WayPoint;

@Entity
public class ReportedRoute extends BaseEntity<Long> {

    private static final long serialVersionUID = -7205030526506222850L;

    // //////////////////////////////////////////////////////////////////////
    // Entity fields (also see super class)
    // //////////////////////////////////////////////////////////////////////
    @NotNull
    private String name;

    @NotNull
    private String enavId;

    @ElementCollection()
    @CollectionTable(name = "ReportedWayPoint")
    @OrderColumn(name = "orderNumber")
    @Valid
    private List<ReportedWayPoint> wayPoints = new ArrayList<>();


    // //////////////////////////////////////////////////////////////////////
    // business logic
    // //////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////
    public void addWayPoint(ReportedWayPoint wPoint) {
        wayPoints.add(wPoint);
    }

    public static ReportedRoute fromModel(Route route) {
        ReportedRoute result = new ReportedRoute(route.getEnavId(), route.getName());

        for(WayPoint wp : route.getWayPoints()){
            ReportedWayPoint rwp = ReportedWayPoint.fromModel(wp);
            result.addWayPoint(rwp);
        }
        
        return result;
    }
    
    public String getWayPointsAsString(){
        StringBuilder builder = new StringBuilder();
        
        for(ReportedWayPoint rwp : wayPoints){
            if(builder.length() > 0){
                builder.append(",  ");
            }
            builder.append("[").append(rwp.getPosition().getLatitudeAsString()).append(",");
            builder.append(rwp.getPosition().getLongitudeAsString()).append("]");
        }
        
        return builder.toString();
        
    }

//    public dk.dma.embryo.rest.json.Route toJsonModel() {
//        Date departure = this.getEtaOfDeparture() == null ? null : this.getEtaOfDeparture().toDate();
//
//        dk.dma.embryo.rest.json.Route toRoute = new dk.dma.embryo.rest.json.Route(this.enavId);
//        toRoute.setName(this.name);
//        toRoute.setDep(this.origin);
//        toRoute.setDes(this.destination);
//        toRoute.setEtaDep(departure);
//        for (WayPoint wp : this.getWayPoints()) {
//            toRoute.getWps().add(wp.toJsonModel());
//        }
//
//        return toRoute;
//    }

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public ReportedRoute() {
    }

    public ReportedRoute(String key, String name) {
        super();
        this.enavId = key;
        this.name = name;
    }

    // //////////////////////////////////////////////////////////////////////
    // Object methods
    // //////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
        return "ReportedRoute [enavId=" + enavId + ", name=" + name + ", wayPoints" + wayPoints + "]";
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

    public List<ReportedWayPoint> getWayPoints() {
        return wayPoints;
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
