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
package dk.dma.embryo.weather.model;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;


/**
 * @author Jesper Tejlgaard
 */
public class DistrictForecast {
    
    private String name;
    private String forecast;
    private String waves;
    private String ice;
    
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

    public DistrictForecast(String name, String forecast, String waves, String ice) {
        this(name, forecast, waves);
        this.ice = ice;
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
        result = prime * result + ((ice == null) ? 0 : ice.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj){
            return true;
        }
        if (obj == null){
            return false;
        }
        if (getClass() != obj.getClass()){
            return false;
        }
        DistrictForecast other = (DistrictForecast) obj;
        if (forecast == null) {
            if (other.forecast != null){
                return false;
            }
        } else if (!forecast.equals(other.forecast)){
            return false;
        }
        if (name == null) {
            if (other.name != null){
                return false;
            }
        } else if (!name.equals(other.name)){
            return false;
        }
        if (waves == null) {
            if (other.waves != null){
                return false;
            }
        } else if (!waves.equals(other.waves)){
            return false;
        }
        if (ice == null) {
            if (other.ice != null) {
                return false;
            }
        } else if (!ice.equals(other.ice)) {
            return false;
        }
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

    public String getIce() {
        return ice;
    }

    public void setIce(String ice) {
        this.ice = ice;
    }
}
