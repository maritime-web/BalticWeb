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

import com.fasterxml.jackson.annotation.JsonInclude;

public class VesselOverview {

    private Double angle;
    private Double x;
    private Double y;
    private String name;
    private String type;
    private long mmsi;
    private String callSign;
    private boolean moored;
    private boolean inAW;

    /**
     * Only one of "sog", "ssog" or "awsog" is set at a time.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double sog;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double ssog;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double awsog;

    // //////////////////////////////////////////////////////////////////////
    // Object methods
    // //////////////////////////////////////////////////////////////////////
    @Override
    public int hashCode() {
        int result = angle != null ? angle.hashCode() : 0;
        result = 31 * result + (x != null ? x.hashCode() : 0);
        result = 31 * result + (y != null ? y.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (int) (mmsi ^ (mmsi >>> 32));
        result = 31 * result + (callSign != null ? callSign.hashCode() : 0);
        result = 31 * result + (moored ? 1 : 0);
        result = 31 * result + (inAW ? 1 : 0);
        result = 31 * result + (sog != null ? sog.hashCode() : 0);
        result = 31 * result + (ssog != null ? ssog.hashCode() : 0);
        result = 31 * result + (awsog != null ? awsog.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "VesselOverview{" +
                "angle=" + angle +
                ", x=" + x +
                ", y=" + y +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", mmsi=" + mmsi +
                ", callSign='" + callSign + '\'' +
                ", moored=" + moored +
                ", inAW=" + inAW +
                ", sog=" + sog +
                ", ssog=" + ssog +
                ", awsog=" + awsog +
                '}';
    }

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
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

    public Double getAngle() {
        return angle;
    }

    public void setAngle(Double angle) {
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
