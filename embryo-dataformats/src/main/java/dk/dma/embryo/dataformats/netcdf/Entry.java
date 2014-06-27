/* Copyright (c) 2014 Danish Maritime Authority
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

import org.joda.time.DateTime;

/**
 * An Entry is a combination of a latitude, longitude, a date/time and an
 * observation.
 * 
 * As this data will contain a lot of duplicates regarding the three first
 * variables, the SmallEntry class can be used to provide a slimmer version that
 * only contains integer references to the beforementioned variables.
 * 
 * @author avlund
 *
 */
class Entry {
    public Entry(double lat, double lon, DateTime dateTime, float observation) {
        this.lat = lat;
        this.lon = lon;
        this.dateTime = dateTime;
        this.observation = observation;
    }

    double lat, lon;
    DateTime dateTime;
    float observation;

    @Override
    public String toString() {
        return "Lat: " + lat + ", lon: " + lon + ", date/time: " + dateTime + ", observation: " + observation;
    }
}