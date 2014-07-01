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
