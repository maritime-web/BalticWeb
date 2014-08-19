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
package dk.dma.embryo.vessel.model;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

import dk.dma.enav.model.voyage.RouteLeg.Heading;

@Embeddable
public class RouteLeg implements Serializable {

    private static final long serialVersionUID = -7205030526506222850L;

    // //////////////////////////////////////////////////////////////////////
    // Entity fields (also see super class)
    // //////////////////////////////////////////////////////////////////////
    /** Speed in knots. */
    @NotNull
    private Double speed;

    /** Heading. */
    private Heading heading;
    
    /** Port XTD. Currently not used but saved in database such that routes can be imported/exported */
    private Double xtdPort;

    /** Starboard XTD. Currently not used but saved in database such that routes can be imported/exported */
    private Double xtdStarboard;

    /** Safe Haven Width Currently not used Currently not used but saved in database such that routes can be imported/exported */
    private Double SFWidth;

    /** Safe Haven Length Currently not used but saved in database such that routes can be imported/exported */
    private Double SFLen;


    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////
    public static RouteLeg fromEnavModel(dk.dma.enav.model.voyage.RouteLeg leg){
        return new RouteLeg(leg.getSpeed(), leg.getXtdPort(), leg.getXtdStarboard(), leg.getHeading());
    }
    
    public dk.dma.enav.model.voyage.RouteLeg toEnavModel(){
        dk.dma.enav.model.voyage.RouteLeg toLeg = new dk.dma.enav.model.voyage.RouteLeg();
        toLeg.setSpeed(this.getSpeed());
        toLeg.setXtdPort(this.getXtdPort());
        toLeg.setXtdStarboard(this.getXtdStarboard());
        toLeg.setHeading(this.getHeading());
        return toLeg;
    }

    public static RouteLeg fromJsonModel(dk.dma.embryo.vessel.json.Waypoint wp){
        RouteLeg result = new RouteLeg();
        result.setHeading(wp.getHeading());
        result.setSpeed(wp.getSpeed());
        return result;
        
    }
    
    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public RouteLeg() {
    }

    public RouteLeg(Double speed, Double xtdPort, Double xtdStarboard, Heading heading) {
        super();
        this.speed = speed;
        this.xtdPort = xtdPort;
        this.xtdStarboard = xtdStarboard;
        this.heading = heading;
    }


    // //////////////////////////////////////////////////////////////////////
    // Object methods
    // //////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Double getXtdPort() {
        return xtdPort;
    }

    public void setXtdPort(Double xtdPort) {
        this.xtdPort = xtdPort;
    }

    public Double getXtdStarboard() {
        return xtdStarboard;
    }

    public void setXtdStarboard(Double xtdStarboard) {
        this.xtdStarboard = xtdStarboard;
    }

    public Double getSFWidth() {
        return SFWidth;
    }

    public void setSFWidth(Double sFWidth) {
        SFWidth = sFWidth;
    }

    public Double getSFLen() {
        return SFLen;
    }

    public void setSFLen(Double sFLen) {
        SFLen = sFLen;
    }

    public Heading getHeading() {
        return heading;
    }

    public void setHeading(Heading heading) {
        this.heading = heading;
    }
    
}
