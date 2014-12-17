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

import dk.dma.arcticweb.reporting.json.model.GreenPos;
import dk.dma.arcticweb.reporting.json.model.GreenPosShort;
import dk.dma.embryo.common.persistence.BaseEntity;
import dk.dma.embryo.vessel.model.Position;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/**
 * @author Jesper Tejlgaard
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NamedQueries({
        @NamedQuery(name = "GreenPosReport:findLatest", query = "SELECT DISTINCT g FROM GreenPosReport g where g.vesselMmsi = :vesselMmsi ORDER By g.ts DESC"),
        @NamedQuery(name = "GreenPosReport:findById", query = "SELECT g FROM GreenPosReport g where g.enavId = :id")})
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

    private String vesselMalFunctions;

    @NotNull
    private String reportedBy;

    @NotNull
    private String recipient;

    private Integer number;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @NotNull
    private DateTime ts;

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

    public GreenPosReport(String vesselName, Long vesselMmsi, String vesselCallSign, String latitude, String longitude, Integer number, String vesselMalFunctions) {
        this(vesselName, vesselMmsi, vesselCallSign, new Position(latitude, longitude), number, vesselMalFunctions);
    }

    public GreenPosReport(String vesselName, Long vesselMmsi, String vesselCallSign, Position position, Integer number, String vesselMalFunctions) {
        this();
        this.vesselName = vesselName;
        this.vesselMmsi = vesselMmsi;
        this.vesselCallSign = vesselCallSign;
        this.position = position;
        this.vesselMalFunctions = vesselMalFunctions;
        this.number = number;
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

    public DateTime getTs() {
        return ts;
    }

    public void setTs(DateTime ts) {
        this.ts = ts;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getVesselMalFunctions() {
        return vesselMalFunctions;
    }

    public void setVesselMalFunctions(String vesselMalFunctions) {
        this.vesselMalFunctions = vesselMalFunctions;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }
}
