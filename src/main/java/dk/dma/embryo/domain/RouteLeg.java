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

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

@Embeddable
public class RouteLeg implements Serializable {

    private static final long serialVersionUID = -7205030526506222850L;

    // //////////////////////////////////////////////////////////////////////
    // Entity fields (also see super class)
    // //////////////////////////////////////////////////////////////////////
    /** Speed in knots. */
    @NotNull
    private Double speed;

    /** Port XTD. */
    @NotNull
    private Double xtdPort;

    /** Starboard XTD. */
    @NotNull
    private Double xtdStarboard;

    /** Safe Haven Width */
    private Double SFWidth;

    /** Safe Haven Length */
    private Double SFLen;

    // //////////////////////////////////////////////////////////////////////
    // business logic
    // //////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////
    public static RouteLeg from(dk.dma.enav.model.voyage.RouteLeg leg){
        return new RouteLeg(leg.getSpeed(), leg.getXtdPort(), leg.getXtdStarboard());
    }
    
    public dk.dma.enav.model.voyage.RouteLeg toEnavModel(){
        dk.dma.enav.model.voyage.RouteLeg toLeg = new dk.dma.enav.model.voyage.RouteLeg();
        toLeg.setSpeed(this.getSpeed());
        toLeg.setXtdPort(this.getXtdPort());
        toLeg.setXtdStarboard(this.getXtdStarboard());
        return toLeg;
    }
    
    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public RouteLeg() {
    }

    
    public RouteLeg(Double speed, Double xtdPort, Double xtdStarboard) {
        super();
        this.speed = speed;
        this.xtdPort = xtdPort;
        this.xtdStarboard = xtdStarboard;
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

    
}
