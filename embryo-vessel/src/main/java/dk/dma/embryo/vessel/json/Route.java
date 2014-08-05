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
