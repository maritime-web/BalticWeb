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
import org.joda.time.LocalDateTime;

/**
 * 
 * @author Jesper Tejlgaard
 */
public class GreenposSearch {

    // //////////////////////////////////////////////////////////////////////
    // POJO fields
    // //////////////////////////////////////////////////////////////////////
    private String reportType;

    private String shipName;

    private Long shipMmsi;

    private String shipCallSign;

    private String reportedBy;

    private LocalDateTime ts;

    private String sortByField = "ts";

    private String sortOrder = "DESC";
    
    private Integer first;

    private Integer numberOfReports;

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public GreenposSearch() {
    }

    public GreenposSearch(String reportType, String shipName, Long shipMmsi, String shipCallSign, String reportedBy,
            LocalDateTime ts, String sortBy, String sortOrder, Integer first, Integer numberOfReports) {
        super();
        
        if(sortOrder != null && sortOrder.trim().length() > 0){
            this.sortOrder = sortOrder;
        }
        
        if(sortBy != null && sortBy.trim().length() > 0){
            this.sortByField = sortBy;
        }

        if(!"ASC".equals(this.sortOrder) && !"DESC".equals(this.sortOrder)){
            throw new IllegalArgumentException("Invalid value '" + sortOrder + "'. sortOrder can have values 'ASC' and 'DESC'");
        }
        
        this.reportType = reportType;
        this.shipName = shipName;
        this.shipMmsi = shipMmsi;
        this.shipCallSign = shipCallSign;
        this.reportedBy = reportedBy;
        this.ts = ts;
        this.first = first;
        this.numberOfReports = numberOfReports;
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
    public String getShipName() {
        return shipName;
    }

    public String getShipCallSign() {
        return shipCallSign;
    }

    public Long getShipMmsi() {
        return shipMmsi;
    }
    public String getReportedBy() {
        return reportedBy;
    }

    public LocalDateTime getTs() {
        return ts;
    }
    public String getReportType() {
        return reportType;
    }

    public String getSortByField() {
        System.out.println(sortByField);
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
