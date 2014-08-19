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
package dk.dma.arcticweb.reporting.json.model;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import dk.dma.embryo.vessel.json.ActiveRoute;

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
