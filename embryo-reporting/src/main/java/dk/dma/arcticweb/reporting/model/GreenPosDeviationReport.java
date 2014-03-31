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
package dk.dma.arcticweb.reporting.model;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import dk.dma.arcticweb.reporting.json.model.GreenPos;
import dk.dma.arcticweb.reporting.json.model.GreenPosShort;
import dk.dma.embryo.vessel.model.Position;

/**
 * Deviation may be reported as either a free form textual description {@link #deviation} or a modified voyage plan
 * or a combination of both.
 * 
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

    @OneToOne(cascade=CascadeType.PERSIST)
    private ReportedRoute route;
    
    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////
    public static GreenPosDeviationReport fromJsonModel(GreenPos from) {
        Position pos = new Position(from.getLat(), from.getLon());

        GreenPosDeviationReport report = new GreenPosDeviationReport(from.getVesselName(), from.getMmsi(),
                from.getCallSign(), pos, from.getDescription());

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
        result.setDescription(getDeviation());
        result.setReporter(getReportedBy());
        result.setTs(getTs().toDate());
        
        return result;
    }

    @Override
    public GreenPosShort toJsonModelShort() {
        GreenPosShort result = new GreenPosShort();
        result.setId(getEnavId());
        result.setType(getReportType());
        result.setLon(getPosition().getLongitudeAsString());
        result.setLat(getPosition().getLatitudeAsString());
        result.setDeviation(getDeviation());
        result.setTs(getTs().toDate());
        
        return result;
    }
    
    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public GreenPosDeviationReport() {
        super();
    }

    public GreenPosDeviationReport(String vesselName, Long vesselMmsi, String vesselCallSign,
            Position pos, String deviation) {
        super(vesselName, vesselMmsi, vesselCallSign, pos);

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
