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
import dk.dma.embryo.vessel.model.Position;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * Deviation may be reported as either a free form textual description {@link #deviation} or a modified voyage plan
 * or a combination of both.
 * <p/>
 * The system is expected to insert a modified voyage plan if it exists. The textual description is filled in by either
 * vessel or authorities (Arctic Command).
 *
 * @author Jesper Tejlgaard
 */
@Entity
@DiscriminatorValue("DR")
public class GreenPosDeviationReport extends GreenPosReport {

    private static final long serialVersionUID = -7205030526506222850L;

    private String deviation;

    @OneToOne(cascade = CascadeType.PERSIST)
    private ReportedRoute route;

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////
    public static GreenPosDeviationReport fromJsonModel(GreenPos from) {
        Position pos = new Position(from.getLat(), from.getLon());

        GreenPosDeviationReport report = new GreenPosDeviationReport(from.getVesselName(), from.getMmsi(),
                from.getCallSign(), pos, from.getNumber(), from.getDescription(), from.getMalFunctions());

        return report;
    }

    @Override
    public GreenPos toJsonModel() {
        GreenPos result = new GreenPos();
        result.setId(getEnavId());
        result.setType(getReportType());
        result.setVesselName(getVesselName());
        result.setMmsi(getVesselMmsi());
        result.setCallSign(getVesselCallSign());
        result.setLon(getPosition().getLongitude());
        result.setLat(getPosition().getLatitude());
        result.setNumber(getNumber());
        result.setDescription(getDeviation());
        result.setReporter(getReportedBy());
        result.setTs(getTs().toDate());
        result.setRecipient(getRecipient());

        return result;
    }

    @Override
    public GreenPosShort toJsonModelShort() {
        GreenPosShort result = new GreenPosShort();
        result.setId(getEnavId());
        result.setType(getReportType());
        result.setLon(getPosition().getLongitudeAsString());
        result.setLat(getPosition().getLatitudeAsString());
        result.setNumber(getNumber());
        result.setDeviation(getDeviation());
        result.setTs(getTs().toDate());
        result.setRecipient(getRecipient());
        result.setMalFunctions(getVesselMalFunctions());
        return result;
    }

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public GreenPosDeviationReport() {
        super();
    }

    public GreenPosDeviationReport(String vesselName, Long vesselMmsi, String vesselCallSign,
                                   Position pos, Integer number, String deviation, String vesselMalFunctions) {
        super(vesselName, vesselMmsi, vesselCallSign, pos, number, vesselMalFunctions);

        this.deviation = deviation;
        // this.modifiedPlan = deviatedPlan;
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

    public ReportedRoute getRoute() {
        return route;
    }

    public void setRoute(ReportedRoute route) {
        this.route = route;
    }
}
