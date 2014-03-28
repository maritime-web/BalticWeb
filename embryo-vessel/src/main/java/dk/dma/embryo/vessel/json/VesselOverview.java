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
package dk.dma.embryo.vessel.json;

public class VesselOverview {
    private double angle;
    private double x;
    private double y;
    private String name;
    private String type;
    private long mmsi;
    private String callSign;
    private boolean moored;
    private boolean inAW;
    
    /**
     * The maximum speed over ground recorded from Historical track of the vessel
     */
    private Double msog;

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getMmsi() {
        return mmsi;
    }

    public void setMmsi(Long mmsi) {
        this.mmsi = mmsi;
    }

    public String getCallSign() {
        return callSign;
    }

    public void setCallSign(String callSign) {
        this.callSign = callSign;
    }

    public boolean isMoored() {
        return moored;
    }

    public void setMoored(boolean moored) {
        this.moored = moored;
    }

    public boolean isInAW() {
        return inAW;
    }

    public void setInAW(boolean inArcticWeb) {
        this.inAW = inArcticWeb;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public Double getMsog() {
        return msog;
    }

    public void setMsog(Double msog) {
        this.msog = msog;
    }
    
}
