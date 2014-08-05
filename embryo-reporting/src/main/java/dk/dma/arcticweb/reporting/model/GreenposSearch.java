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
