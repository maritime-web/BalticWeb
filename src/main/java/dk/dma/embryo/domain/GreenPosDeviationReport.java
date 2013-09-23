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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.joda.time.DateTimeZone;

import dk.dma.embryo.rest.json.GreenPos;

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
@DiscriminatorValue("DR")
public class GreenPosDeviationReport extends GreenPosReport {

    private static final long serialVersionUID = -7205030526506222850L;

    private String deviation;

    // @Valid
    // @OneToMany(cascade=CascadeType.ALL)
    // @OrderColumn(name = "orderNumber")
    // private List<ReportedVoyage> modifiedPlan = new ArrayList<>();

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////
    public static GreenPosDeviationReport fromJsonModel(GreenPos from) {
        Position pos = new Position(from.getLatitude(), from.getLongitude());

        GreenPosDeviationReport report = new GreenPosDeviationReport(from.getShipName(), from.getShipMmsi(),
                from.getShipCallSign(), from.getShipMaritimeId(), pos, from.getDeviation());

        return report;
    }

    @Override
    public GreenPos toJsonModel() {
        GreenPos result = new GreenPos();
        result.setReportType(getReportType());
        result.setShipName(getShipName());
        result.setShipMaritimeId(getShipMaritimeId());
        result.setShipMmsi(getShipMmsi());
        result.setShipCallSign(getShipCallSign());
        result.setLongitude(getPosition().getLongitudeAsString());
        result.setLatitude(getPosition().getLatitudeAsString());
        result.setDeviation(getDeviation());
        result.setReportedBy(getReportedBy());
        result.setReportedTs(getTs().toDateTime(DateTimeZone.UTC).getMillis());
        
        return result;
    }

    
    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public GreenPosDeviationReport() {
        super();
    }

    public GreenPosDeviationReport(String shipName, Long shipMmsi, String shipCallSign, String shipMaritimeId,
            Position pos, String deviation) {
        super(shipName, shipMmsi, shipCallSign, shipMaritimeId, pos);

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

    // public List<ReportedVoyage> getModifiedPlan() {
    // return modifiedPlan;
    // }
}
