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

import java.util.List;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDateTime;

import dk.dma.embryo.rest.json.GreenPos;
import dk.dma.embryo.rest.json.GreenPosShort;

/**
 * 
 * @author Jesper Tejlgaard
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NamedQueries({
        @NamedQuery(name = "GreenPosReport:findLatest", query = "SELECT DISTINCT g FROM GreenPosReport g where g.vesselMmsi = :vesselMmsi ORDER By g.ts DESC"),
        @NamedQuery(name = "GreenPosReport:findById", query = "SELECT g FROM GreenPosReport g where g.enavId = :id") })
public abstract class GreenPosReport extends BaseEntity<Long> {

    private static final long serialVersionUID = -7205030526506222850L;

    // //////////////////////////////////////////////////////////////////////
    // Entity fields (also see super class)
    // //////////////////////////////////////////////////////////////////////
    @NotNull
    private String enavId;

    @NotNull
    private String vesselName;

    @NotNull
    private Long vesselMmsi;

    @NotNull
    private String vesselCallSign;

    @Valid
    private Position position;

    @NotNull
    private String reportedBy;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
    @NotNull
    private LocalDateTime ts;

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////

    public static GreenPosReport from(GreenPos from) {
        switch (from.getType()) {
        case "SP":
            return GreenPosSailingPlanReport.fromJsonModel(from);
        case "PR":
            return GreenPosPositionReport.fromJsonModel(from);
        case "FR":
            return GreenPosFinalReport.fromJsonModel(from);
        case "DR":
            return GreenPosDeviationReport.fromJsonModel(from);
        default:
            throw new IllegalArgumentException("Unknown value '" + from.getType() + "' for reportType");
        }
    }

    public static GreenPos[] toJsonModel(List<GreenPosReport> reports) {
        GreenPos[] result = new GreenPos[reports.size()];
        int index = 0;
        for (GreenPosReport report : reports) {
            result[index++] = report.toJsonModel();
        }

        return result;
    }

    public static GreenPosShort[] toJsonModelShort(List<GreenPosReport> reports) {
        GreenPosShort[] result = new GreenPosShort[reports.size()];
        int index = 0;
        for (GreenPosReport report : reports) {
            result[index++] = report.toJsonModelShort();
        }

        return result;
    }

    public abstract GreenPos toJsonModel();

    public abstract GreenPosShort toJsonModelShort();

    public String getReportType() {
        if (getClass() == GreenPosSailingPlanReport.class) {
            return "SP";
        } else if (getClass() == GreenPosPositionReport.class) {
            return "PR";
        } else if (getClass() == GreenPosFinalReport.class) {
            return "FR";
        } else if (getClass() == GreenPosDeviationReport.class) {
            return "DR";
        }
        throw new IllegalStateException("Unknown GreenPosReport instance class. '" + getClass().getName() + "'.");
    }

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
    public GreenPosReport() {
        this.enavId = UUID.randomUUID().toString();
    }

    public GreenPosReport(String vesselName, Long vesselMmsi, String vesselCallSign, String latitude, String longitude) {
        this();
        this.vesselName = vesselName;
        this.vesselMmsi = vesselMmsi;
        this.vesselCallSign = vesselCallSign;
        this.position = new Position(latitude, longitude);
    }

    public GreenPosReport(String vesselName, Long vesselMmsi, String vesselCallSign, Position position) {
        this();
        this.vesselName = vesselName;
        this.vesselMmsi = vesselMmsi;
        this.vesselCallSign = vesselCallSign;
        this.position = position;
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
    public String getVesselName() {
        return vesselName;
    }

    public String getEnavId() {
        return enavId;
    }

    public String getVesselCallSign() {
        return vesselCallSign;
    }

    public Long getVesselMmsi() {
        return vesselMmsi;
    }

    public Position getPosition() {
        return position;
    }

    public void setReportedBy(String userName) {
        this.reportedBy = userName;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public LocalDateTime getTs() {
        return ts;
    }

    public void setTs(LocalDateTime ts) {
        this.ts = ts;
    }

}
