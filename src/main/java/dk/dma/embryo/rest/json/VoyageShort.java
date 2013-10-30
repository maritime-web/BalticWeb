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

import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * 
 * @author Jesper Tejlgaard
 */
public class VoyageShort {

    // Properties relevant for current functionality. Extra can be added.

    private String maritimeId;

    private String loc;

    private Date arr;

    private Date dep;

    private String routeId;

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public VoyageShort() {
        super();
    }

    public VoyageShort(String maritimeId, String berthName, Date arrival, Date departure) {
        super();
        this.maritimeId = maritimeId;
        this.loc = berthName;
        this.arr = arrival;
        this.dep = departure;
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

    public String getMaritimeId() {
        return maritimeId;
    }

    public void setMaritimeId(String maritimeId) {
        this.maritimeId = maritimeId;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    public Date getArr() {
        return arr;
    }

    public void setArr(Date arr) {
        this.arr = arr;
    }

    public Date getDep() {
        return dep;
    }

    public void setDep(Date dep) {
        this.dep = dep;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

}
