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
package dk.dma.embryo.vessel.json;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * 
 * @author Jesper Tejlgaard
 */
public class ScheduleResponse {

    // Properties relevant for current functionality. Extra can be added.

    private Voyage[] voyages;
    
    private String[] errors;

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public ScheduleResponse() {
        super();
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
    public Voyage[] getVoyages() {
        return voyages;
    }

    public void setVoyages(Voyage[] voyages) {
        this.voyages = voyages;
    }
    
    public String[] getErrors() {
        return errors;
    }
    
    public void setErrors(String[] errors) {
        this.errors = errors;
    }
}
