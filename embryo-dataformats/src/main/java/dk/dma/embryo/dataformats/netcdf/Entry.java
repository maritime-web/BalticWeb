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
