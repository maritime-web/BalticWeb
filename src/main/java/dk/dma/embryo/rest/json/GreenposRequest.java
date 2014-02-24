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
package dk.dma.embryo.rest.json;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public class GreenposRequest {

    private ActiveRoute activeRoute;

    private GreenPos report;

    private Boolean includeActiveRoute;

    
    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public GreenposRequest() {
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
    public ActiveRoute getActiveRoute() {
        return activeRoute;
    }

    public void setActiveRoute(ActiveRoute activeRoute) {
        this.activeRoute = activeRoute;
    }

    public GreenPos getReport() {
        return report;
    }

    public void setReport(GreenPos report) {
        this.report = report;
    }

    public Boolean getIncludeActiveRoute() {
        return includeActiveRoute;
    }

    public void setIncludeActiveRoute(Boolean includeActiveRoute) {
        this.includeActiveRoute = includeActiveRoute;
    }
}
