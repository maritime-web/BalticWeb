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

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.joda.time.DateTime;

/**
 * 
 * @author Jesper Tejlgaard
 */
public class GreenposSearch {

    // //////////////////////////////////////////////////////////////////////
    // POJO fields
    // //////////////////////////////////////////////////////////////////////
    private String reportType;

    private String vesselName;

    private Long vesselMmsi;

    private String vesselCallSign;

    private String reportedBy;

    private DateTime ts;

    private String sortByField = "ts";

    private String sortOrder = "DESC";

    private Integer first;

    private Integer numberOfReports;

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public GreenposSearch() {
    }

    public GreenposSearch(String reportType, String vesselName, Long vesselMmsi, String vesselCallSign,
            String reportedBy, DateTime ts, String sortBy, String sortOrder, Integer first, Integer numberOfReports) {
        super();

        if (sortOrder != null && sortOrder.trim().length() > 0) {
            this.sortOrder = sortOrder;
        }

        if (sortBy != null && sortBy.trim().length() > 0) {
            this.sortByField = sortBy;
        }

        if (!"ASC".equals(this.sortOrder) && !"DESC".equals(this.sortOrder)) {
            throw new IllegalArgumentException("Invalid value '" + sortOrder
                    + "'. sortOrder can have values 'ASC' and 'DESC'");
        }

        this.reportType = reportType;
        this.vesselName = vesselName;
        this.vesselMmsi = vesselMmsi;
        this.vesselCallSign = vesselCallSign;
        this.reportedBy = reportedBy;
        this.ts = ts;
        this.first = first;
        this.numberOfReports = numberOfReports;
    }

    public GreenposSearch(String reportType, Long vesselMmsi, DateTime ts, String sortBy, String sortOrder,
            Integer first, Integer numberOfReports) {
        this(reportType, null, vesselMmsi, null, null, ts, sortBy, sortOrder, first, numberOfReports);

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

    public String getVesselCallSign() {
        return vesselCallSign;
    }

    public Long getVesselMmsi() {
        return vesselMmsi;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public DateTime getTs() {
        return ts;
    }

    public String getReportType() {
        return reportType;
    }

    public String getSortByField() {
        return sortByField;
    }

    public int getFirst() {
        return first == null ? 0 : first;
    }

    public int getNumberOfReports() {
        return numberOfReports != null ? numberOfReports : 0;
    }

    public String getSortOrder() {
        return sortOrder;
    }

}
