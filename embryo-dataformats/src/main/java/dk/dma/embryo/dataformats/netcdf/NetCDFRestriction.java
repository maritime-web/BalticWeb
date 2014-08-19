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
package dk.dma.embryo.dataformats.netcdf;

/**
 * Restricts the range of the dimensions used in parsing.
 * 
 * @author avlund
 *
 */
public class NetCDFRestriction {
    private int minLat;
    private int maxLat;
    private int minLon;
    private int maxLon;
    private int timeStart;
    private int timeInterval = 1;
    public int getMinLat() {
        return minLat;
    }
    public void setMinLat(int minLat) {
        this.minLat = minLat;
    }
    public int getMaxLat() {
        return maxLat;
    }
    public void setMaxLat(int maxLat) {
        this.maxLat = maxLat;
    }
    public int getMinLon() {
        return minLon;
    }
    public void setMinLon(int minLon) {
        this.minLon = minLon;
    }
    public int getMaxLon() {
        return maxLon;
    }
    public void setMaxLon(int maxLon) {
        this.maxLon = maxLon;
    }
    public int getTimeStart() {
        return timeStart;
    }
    public void setTimeStart(int timeStart) {
        this.timeStart = timeStart;
    }
    public int getTimeInterval() {
        return timeInterval;
    }
    public void setTimeInterval(int timeInterval) {
        this.timeInterval = timeInterval;
    }
}
