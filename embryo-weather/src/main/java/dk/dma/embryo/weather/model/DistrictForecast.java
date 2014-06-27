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
package dk.dma.embryo.weather.model;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;


/**
 * @author Jesper Tejlgaard
 */
public class DistrictForecast {
    
    private String name;
    private String forecast;
    private String waves;
    
    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public DistrictForecast() {
        super();
    }
    
    public DistrictForecast(String name, String forecast, String waves) {
        super();
        this.name = name;
        this.forecast = forecast;
        this.waves = waves;
    }

    // //////////////////////////////////////////////////////////////////////
    // Object methods
    // //////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((forecast == null) ? 0 : forecast.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((waves == null) ? 0 : waves.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DistrictForecast other = (DistrictForecast) obj;
        if (forecast == null) {
            if (other.forecast != null)
                return false;
        } else if (!forecast.equals(other.forecast))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (waves == null) {
            if (other.waves != null)
                return false;
        } else if (!waves.equals(other.waves))
            return false;
        return true;
    }

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getForecast() {
        return forecast;
    }


    public void setForecast(String forecast) {
        this.forecast = forecast;
    }


    public String getWaves() {
        return waves;
    }


    public void setWaves(String waves) {
        this.waves = waves;
    }    
}
