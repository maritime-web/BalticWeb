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
package dk.dma.embryo.vessel.json;


public class VesselOverview {
    
    private Double angle;
    private Double x;
    private Double y;
    private String name;
    private String type;
    private Long mmsi;
    private String callSign;
    private Boolean moored;
    private Boolean inAW;
    
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((angle == null)      ? 0 : angle.hashCode());
        result = prime * result + ((x == null)          ? 0 : x.hashCode());
        result = prime * result + ((y == null)          ? 0 : y.hashCode());
        result = prime * result + ((name == null)       ? 0 : name.hashCode());
        result = prime * result + ((type == null)       ? 0 : type.hashCode());
        result = prime * result + ((mmsi == null)       ? 0 : mmsi.hashCode());
        result = prime * result + ((callSign == null)   ? 0 : callSign.hashCode());
        result = prime * result + ((moored == null)     ? 0 : moored.hashCode());
        result = prime * result + ((inAW == null)       ? 0 : inAW.hashCode());
        
        return result;
    }
    
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
