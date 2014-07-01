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

import java.io.Serializable;

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
    private float observation;

    public SmallEntry(int lat, int lon, int time, float observation) {
        this.lat = lat;
        this.lon = lon;
        this.time = time;
        this.observation = observation;
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

    public float getObservation() {
        return observation;
    }
}
