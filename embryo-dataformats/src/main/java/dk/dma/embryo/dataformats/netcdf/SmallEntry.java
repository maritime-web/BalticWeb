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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A SmallEntry is an entry that instead of complete information only contains
 * integer references to latitudes, longitudes and DateTime objects found in the
 * metadata. This ensures a smaller footprint and thus less data to send over
 * the wire, but the data needs to be augmented with the metadata in order to be
 * useful.
 * 
 * Contrast this with the Entry class, where each object contains all necessary
 * information to provide relevant observation details.
 * 
 * @author avlund
 *
 */
public class SmallEntry implements Serializable {
    private static final long serialVersionUID = 5778890617311697112L;

    private int lat, lon, time;
    private Map<Integer, Float> obs;

    public SmallEntry(int lat, int lon, int time, int obsNo, float observation) {
        this.lat = lat;
        this.lon = lon;
        this.time = time;
        this.obs = new HashMap<>();
        obs.put(obsNo, observation);
    }

    public int getLat() {
        return lat;
    }

    public int getLon() {
        return lon;
    }

    public int getTime() {
        return time;
    }

    public Map<Integer, Float> getObs() {
        return obs;
    }
    
    @Override
    public String toString() {
        return "Lat: " + lat + ", lon: " + lon + ", time: " + time + ", obs: " + obs;
    }
}
