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

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * 
 * @author Jesper Tejlgaard
 */
public class ScheduleRequest {

    /**
     * MMSI of vessel for which to update the schedule
     */
    private Long mmsi;
    
    /**
     * Updated/new voyages to persist in the database
     */
    private Voyage[] voyages;

    /**
     * Voyage IDs of voyages to delete. 
     */
    private String[] toDelete;
    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public ScheduleRequest() {
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

    public String[] getToDelete() {
        return toDelete;
    }

    public void setToDelete(String[] toDelete) {
        this.toDelete = toDelete;
    }

    public Long getMmsi() {
        return mmsi;
    }

    public void setMmsi(Long mmsi) {
        this.mmsi = mmsi;
    }
    
    
}
