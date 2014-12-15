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

import dk.dma.embryo.vessel.model.Position;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

/**
 * @author Jesper Tejlgaard
 */
@Entity
public abstract class GreenPosDMIReport extends GreenPosReport {

    private static final long serialVersionUID = -7205030526506222850L;

    // //////////////////////////////////////////////////////////////////////
    // Entity fields (also see super class)
    // //////////////////////////////////////////////////////////////////////
    @NotNull
    private String weather;

    @NotNull
    private String iceInformation;

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public GreenPosDMIReport() {
        super();
    }

    public GreenPosDMIReport(String vesselName, Long vesselMmsi, String vesselCallSign,
                             String latitude, String longitude, Integer number, String weather, String iceInformation, String vesselMalFunctions) {
        super(vesselName, vesselMmsi, vesselCallSign, latitude, longitude, number, vesselMalFunctions);
        this.weather = weather;
        this.iceInformation = iceInformation;
    }

    public GreenPosDMIReport(String vesselName, Long vesselMmsi, String vesselCallSign,
                             Position position, Integer number, String weather, String iceInformation, String vesselMalFunctions) {
        super(vesselName, vesselMmsi, vesselCallSign, position, number, vesselMalFunctions);
        this.weather = weather;
        this.iceInformation = iceInformation;
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
    public String getWeather() {
        return weather;
    }

    public String getIceInformation() {
        return iceInformation;
    }
}
