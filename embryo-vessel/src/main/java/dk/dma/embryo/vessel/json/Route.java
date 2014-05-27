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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import dk.dma.enav.model.MaritimeId;

public class Route {

    private final List<Waypoint> waypoints = new ArrayList<>();
    private String name;
    private String destination;
    private String departure;
    private Date etaDep;
    private Date etaDes;

    /** Should this be implemented as a {@link MaritimeId} ? */
    private String id;

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public Route() {
    }

    public Route(String id) {
        this.id = id;
    }

    /**
     * Constructor generating an id
     * 
     * @param name
     * @param departure
     * @param destination
     */
    public Route(String name, String departure, String destination) {
        super();
        // TODO generate id
        this.name = name;
        this.destination = destination;
        this.departure = departure;
    }

    public Route(String id, String name, String departure, String destination) {
        super();
        this.id = id;
        this.name = name;
        this.destination = destination;
        this.departure = departure;
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
    public String getDep() {
        return departure;
    }

    public String getDes() {
        return destination;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public List<Waypoint> getWps() {
        return waypoints;
    }

    public void setDep(String departure) {
        this.departure = departure;
    }

    public void setDes(String destination) {
        this.destination = destination;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getEtaDep() {
        return etaDep;
    }

    public void setEtaDep(Date eta) {
        this.etaDep = eta;
    }

    public Date getEta() {
        return etaDes;
    }

    public void setEta(Date eta) {
        this.etaDes = eta;
    }

}
