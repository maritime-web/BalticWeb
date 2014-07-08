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


/**
 * @author Jesper Tejlgaard
 */
public class Weather {

    private RegionForecast forecast;
    private Warnings warnings;
    
    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public Weather() {
        super();
    }
    
    public Weather(RegionForecast forecast, Warnings warning) {
        super();
        this.forecast = forecast;
        this.warnings = warning;
    }

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public RegionForecast getForecast() {
        return forecast;
    }
    public void setForecast(RegionForecast forecast) {
        this.forecast = forecast;
    }
    public Warnings getWarnings() {
        return warnings;
    }
    public void setWarnings(Warnings warning) {
        this.warnings = warning;
    }
}
