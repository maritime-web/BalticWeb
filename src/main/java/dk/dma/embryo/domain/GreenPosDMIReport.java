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

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * 
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
            String latitude, String longitude, String weather, String iceInformation) {
        super(vesselName, vesselMmsi, vesselCallSign, latitude, longitude);
        this.weather = weather;
        this.iceInformation = iceInformation;
    }

    public GreenPosDMIReport(String vesselName, Long vesselMmsi, String vesselCallSign,
            Position position, String weather, String iceInformation) {
        super(vesselName, vesselMmsi, vesselCallSign, position);
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
