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
package dk.dma.embryo.enav.io;

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
