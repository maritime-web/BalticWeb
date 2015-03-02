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
     * Only one of "sog", "ssog" or "awsog" is set at a time.
     */
    private Double sog;
    private Double ssog;
    private Double awsog;
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        
        long temp;
        temp = Double.doubleToLongBits(angle);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((callSign == null) ? 0 : callSign.hashCode());
        result = prime * result + (inAW ? 1231 : 1237);
        result = prime * result + (int) (mmsi ^ (mmsi >>> 32));
        result = prime * result + (moored ? 1231 : 1237);
        result = prime * result + ((awsog == null) ? 0 : awsog.hashCode());
        result = prime * result + ((ssog == null) ? 0 : ssog.hashCode());
        result = prime * result + ((sog == null) ? 0 : sog.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        temp = Double.doubleToLongBits(x);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        
        return result;
    }
    

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

    public Double getSog() {
        return sog;
    }
    public void setSog(Double sog) {
        this.sog = sog;
    }

    public Double getSsog() {
        return ssog;
    }
    public void setSsog(Double ssog) {
        this.ssog = ssog;
    }

    public Double getAwsog() {
        return awsog;
    }
    public void setAwsog(Double awsog) {
        this.awsog = awsog;
    }
}
