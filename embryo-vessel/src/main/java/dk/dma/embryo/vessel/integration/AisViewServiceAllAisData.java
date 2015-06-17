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
package dk.dma.embryo.vessel.integration;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.util.List;

/**
 * 
 * @author ThomasBerg
 *
 * base URL: http://ais.e-navigation.net/aw8080/target
 *
 */
public interface AisViewServiceAllAisData {
    
    String LOOK_BACK_PT24H      = "PT24H";
    String LOOK_BACK_PT12H      = "PT12H";
    String LOOK_BACK_PT120H     = "PT120H";
    
    /**
     * Fetch the historical tracks from AisTrack for the given vessel
     * example URL: http://ais.e-navigation.net/aw8080/target/vessel/track/219000217?minDist=500&age=PT12H
     * 
     * @param mmsi the MMSI of the vessel
     * @param minimumDistanceBetweenPositions minimum distance between positions
     * @param age the duration in iso-8601 format
     * @return the past track positions
     */
    @GET
    @Path("/vessel/track/{mmsi}")
    List<TrackSingleLocation> historicalTrack(
        @PathParam("mmsi") long mmsi, 
        @QueryParam("minDist") int minimumDistanceBetweenPositions, 
        @QueryParam("age") String age);
    
    /**
     * Fetch the historical tracks from AisStore for the given vessel
     * example URL: http://ais.e-navigation.net/aw8080/target/vessel/longtrack/219000217?minDist=500&age=PT12H
     *
     * @param mmsi the MMSI of the vessel
     * @param minimumDistanceBetweenPositions minimum distance between positions
     * @param age the duration in iso-8601 format
     * @return the past track positions
     */
    @GET
    @Path("/vessel/longtrack/{mmsi}")
    List<TrackSingleLocation> historicalTrackLong(
        @PathParam("mmsi") long mmsi, 
        @QueryParam("minDist") int minimumDistanceBetweenPositions, 
        @QueryParam("age") String age);
    
    /**
     * example URL: http://ais.e-navigation.net:8080/target/vessel/list?ttlLive=PT24H&ttlSat=PT24H
     * 
     * @param ttlLive
     * @param ttlSat
     * @return
     */
    @GET
    @Path("/vessel/list")
    List<AisVessel> vesselList(
        @QueryParam("ttlLive") String ttlLive, 
        @QueryParam("ttlSat") String ttlSat);
    
    /**
     * example URL: http://ais.e-navigation.net/aw8080/target/vessel/maxspeed
     * 
     * @return
     */
    @GET
    @Path("/vessel/maxspeed")
    List<MaxSpeed> allMaxSpeeds();

    public static class TrackSingleLocation {

        private Double cog;
        private Double lat;
        private Double lon;
        private Double sog;
        private Long time;
        
        public TrackSingleLocation() {}
        
        public TrackSingleLocation(Double cog, Double lat, Double lon, Double sog, Long time) {
            super();
            this.cog = cog;
            this.lat = lat;
            this.lon = lon;
            this.sog = sog;
            this.time = time;
        }

        @Override
        public int hashCode() {

            final int prime = 31;
            int result = 1;
            result = prime * result + ((cog == null)  ? 0 : cog.hashCode());
            result = prime * result + ((lat == null)  ? 0 : lat.hashCode());
            result = prime * result + ((lon == null)  ? 0 : lon.hashCode());
            result = prime * result + ((sog == null)  ? 0 : sog.hashCode());
            result = prime * result + ((time == null) ? 0 : time.hashCode());

            return result;
        }

        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this);
        }
        
        public Double getCog() {
            return cog;
        }
        public void setCog(Double cog) {
            this.cog = cog;
        }
        
        public Double getLat() {
            return lat;
        }
        public void setLat(Double lat) {
            this.lat = lat;
        }
        
        public Double getLon() {
            return lon;
        }
        public void setLon(Double lon) {
            this.lon = lon;
        }

        public Double getSog() {
            return sog;
        }
        public void setSog(Double sog) {
            this.sog = sog;
        }

        public Long getTime() {
            return time;
        }
        public void setTime(Long time) {
            this.time = time;
        }
    }
    
    public static class MaxSpeed {
        
        private Long mmsi;
        private Double maxSpeed;
        
        public Long getMmsi() {
            return mmsi;
        }
        public void setMmsi(Long mmsi) {
            this.mmsi = mmsi;
        }

        public Double getMaxSpeed() {
            return maxSpeed;
        }
        public void setMaxSpeed(Double maxSpeed) {
            this.maxSpeed = maxSpeed;
        }
    }
}
