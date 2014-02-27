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
package dk.dma.enav.serialization;

/**
 * @author Jesper Tejlgaard
 */
public class RouteDefaults {

    private static final long serialVersionUID = 1L;
        
    // //////////////////////////////////////////////////////////////////////
    // fields
    // //////////////////////////////////////////////////////////////////////
    private double defaultSpeed = 10.0;
    private double defaultTurnRad = 0.5;
    private double defaultXtd = 0.1;
    
    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public RouteDefaults() {
        super();
    }

    public RouteDefaults(double defaultSpeed, double defaultTurnRad, double defaultXtd) {
        super();
        this.defaultSpeed = defaultSpeed;
        this.defaultTurnRad = defaultTurnRad;
        this.defaultXtd = defaultXtd;
    }

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public double getDefaultSpeed() {
        return defaultSpeed;
    }

    public double getDefaultTurnRad() {
        return defaultTurnRad;
    }

    public double getDefaultXtd() {
        return defaultXtd;
    }
}
