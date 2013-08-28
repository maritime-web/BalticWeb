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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.validation.Valid;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * Deviation may be reported as either a free form textual description {@link #deviation} or a modified voyage plan
 * {@link #modifiedPlan} or a combination of both.
 * 
 * The system is expected to insert a modified voyage plan if it exists. The textual description if filled in by either
 * ship or authorities (Grønlandskommandoen).
 * 
 * @author Jesper Tejlgaard
 */
@Entity
public class GreenPosDeviationReport extends GreenPosReport {

    private static final long serialVersionUID = -7205030526506222850L;

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////

    private String deviation;

//    @Valid
//    @OneToMany(cascade=CascadeType.ALL)
//    @OrderColumn(name = "orderNumber")
//    private List<ReportedVoyage> modifiedPlan = new ArrayList<>();

    // public static Report fromEnavModel(dk.dma.enav.model.voyage.Route from) {
    // Report route = new Report(from.getId(), from.getName(), from.getDeparture(), from.getDestination());
    //
    // for (Waypoint wayPoint : from.getWaypoints()) {
    // route.addWayPoint(WayPoint.fromEnavModel(wayPoint));
    // }
    //
    // return route;
    // }
    //
    // public dk.dma.enav.model.voyage.Route toEnavModel() {
    // dk.dma.enav.model.voyage.Route toRoute = new dk.dma.enav.model.voyage.Route(this.enavId);
    // toRoute.setName(this.name);
    // toRoute.setDeparture(this.origin);
    // toRoute.setDestination(this.destination);
    //
    // for (WayPoint wp : this.getWayPoints()) {
    // toRoute.getWaypoints().add(wp.toEnavModel());
    // }
    //
    // return toRoute;
    // }

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public GreenPosDeviationReport() {
        super();
    }

    public GreenPosDeviationReport(String shipName, Long shipMmsi, String shipCallSign, String shipMaritimeId,
            String latitude, String longitude, String deviation,
            List<ReportedVoyage> deviatedPlan) {
        super(shipName, shipMmsi, shipCallSign, shipMaritimeId, latitude, longitude, null, null);

        this.deviation = deviation;
//        this.modifiedPlan = deviatedPlan;
    }

    public GreenPosDeviationReport(String shipName, Long shipMmsi, String shipCallSign, String shipMaritimeId,
            Position position, String deviation,
            List<ReportedVoyage> deviatedPlan) {
        super(shipName, shipMmsi, shipCallSign, shipMaritimeId, position, null, null);

        this.deviation = deviation;
//        this.modifiedPlan = deviatedPlan;
    }

    // //////////////////////////////////////////////////////////////////////
    // Object methods
    // //////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    public String getDeviation() {
        return deviation;
    }

//    public List<ReportedVoyage> getModifiedPlan() {
//        return modifiedPlan;
//    }
}
