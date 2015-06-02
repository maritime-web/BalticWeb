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
package dk.dma.embryo.metoc.json.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public interface DmiSejlRuteService {
    SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.000Z");

    @POST()
    @Path("/SR")
    SejlRuteResponse sejlRute(@FormParam("req") SejlRuteRequest request);

    public static class ForecastValue {
        private double forecast;

        public double getForecast() {
            return forecast;
        }

        public void setForecast(double forecast) {
            this.forecast = forecast;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Forecast {
        private double lat;
        private double lon;
        private String time;
        
        @JsonProperty("wind-dir")
        private ForecastValue windDir;
        @JsonProperty("wind-speed")
        private ForecastValue windSpeed;
        @JsonProperty("current-dir")
        private ForecastValue currentDir;
        @JsonProperty("current-speed")
        private ForecastValue currentSpeed;
        @JsonProperty("wave-dir")
        private ForecastValue waveDir;
        @JsonProperty("wave-height")
        private ForecastValue waveHeight;
        @JsonProperty("wave-period")
        private ForecastValue wavePeriod;
        @JsonProperty("sealevel")
        private ForecastValue sealevel;

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLon() {
            return lon;
        }

        public void setLon(double lon) {
            this.lon = lon;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public ForecastValue getWindDir() {
            return windDir;
        }

        public void setWindDir(ForecastValue windDir) {
            this.windDir = windDir;
        }

        public ForecastValue getWindSpeed() {
            return windSpeed;
        }

        public void setWindSpeed(ForecastValue windSpeed) {
            this.windSpeed = windSpeed;
        }

        public ForecastValue getCurrentDir() {
            return currentDir;
        }

        public void setCurrentDir(ForecastValue currentDir) {
            this.currentDir = currentDir;
        }

        public ForecastValue getCurrentSpeed() {
            return currentSpeed;
        }

        public void setCurrentSpeed(ForecastValue currentSpeed) {
            this.currentSpeed = currentSpeed;
        }

        public ForecastValue getWaveDir() {
            return waveDir;
        }

        public void setWaveDir(ForecastValue waveDir) {
            this.waveDir = waveDir;
        }

        public ForecastValue getWaveHeight() {
            return waveHeight;
        }

        public void setWaveHeight(ForecastValue waveHeight) {
            this.waveHeight = waveHeight;
        }

        public ForecastValue getWavePeriod() {
            return wavePeriod;
        }

        public void setWavePeriod(ForecastValue wavePeriod) {
            this.wavePeriod = wavePeriod;
        }

        public ForecastValue getSealevel() {
            return sealevel;
        }

        public void setSealevel(ForecastValue sealevel) {
            this.sealevel = sealevel;
        }
    }

    public static class MetocForecast {
        private String created;
        private Forecast[] forecasts;

        public String getCreated() {
            return created;
        }

        public void setCreated(String created) {
            this.created = created;
        }

        public Forecast[] getForecasts() {
            return forecasts;
        }

        public void setForecasts(Forecast[] forecasts) {
            this.forecasts = forecasts;
        }
    }

    /**
     * The following combinations of values have been reported from DMI to be possible:
     * <ul>
     * <li>OK(0, "All ok")</li>
     * <li>WAYPOINTS(1, "Too few waypoints")</li>
     * <li>PARAM(2, "Invalid parameter")</li>
     * <li>DIRECTION(3, "Invalid direction")</li>
     * <li>DATE(5, "Missing or invalid dates")</li>
     * <li>PATH(6, "Invalid path")</li>
     * <li>DATE_INVALID(7, "Invalid dates")</li>
     * <li>DATATYPES(8, "Missing or invalid datatypes")</li>
     * <li>UNKNOWN(9, "Unknown error - sorry!")</li>
     * <li>DELTAT(10, "Problem with delta-T (dt)")</li>
     * </ul>
     */
    public static class SejlRuteResponse {
        private int error;
        private String errorMsg;
        private MetocForecast metocForecast;

        public String toString() {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.writeValueAsString(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public int getError() {
            return error;
        }

        public void setError(int error) {
            this.error = error;
        }

        public MetocForecast getMetocForecast() {
            return metocForecast;
        }

        public void setMetocForecast(MetocForecast metocForecast) {
            this.metocForecast = metocForecast;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        public void setErrorMsg(String errorMsg) {
            this.errorMsg = errorMsg;
        }
    }

    public static class Waypoint {
        private String eta;
        private String heading;
        private double lat;
        private double lon;

        public String getEta() {
            return eta;
        }

        public void setEta(String eta) {
            this.eta = eta;
        }

        public String getHeading() {
            return heading;
        }

        public void setHeading(String heading) {
            this.heading = heading;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLon() {
            return lon;
        }

        public void setLon(double lon) {
            this.lon = lon;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((eta == null) ? 0 : eta.hashCode());
            result = prime * result + ((heading == null) ? 0 : heading.hashCode());
            long temp;
            temp = Double.doubleToLongBits(lat);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(lon);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj){
                return true;
            }
            if (obj == null){
                return false;                
            }
            if (getClass() != obj.getClass()){
                return false;
            }
            Waypoint other = (Waypoint) obj;
            if (eta == null) {
                if (other.eta != null){
                    return false;
                }
            } else if (!eta.equals(other.eta)){
                return false;
            }
            if (heading == null) {
                if (other.heading != null){
                    return false;
                }
            } else if (!heading.equals(other.heading)){
                return false;
            }
            if (Double.doubleToLongBits(lat) != Double.doubleToLongBits(other.lat)){
                return false;
            }
            if (Double.doubleToLongBits(lon) != Double.doubleToLongBits(other.lon)){
                return false;
            }
            return true;
        }
        
        
    }

    public static class SejlRuteRequest {
        private long mssi;
        private String[] datatypes;
        private int dt;
        private Waypoint[] waypoints;

        public String toString() {
            return MetocJsonClientFactory.asJson(this);
        }

        public int getDt() {
            return dt;
        }

        public void setDt(int dt) {
            this.dt = dt;
        }

        public Waypoint[] getWaypoints() {
            return waypoints;
        }

        public void setWaypoints(Waypoint[] waypoints) {
            this.waypoints = waypoints;
        }

        public String[] getDatatypes() {
            return datatypes;
        }

        public void setDatatypes(String[] datatypes) {
            this.datatypes = datatypes;
        }

        public long getMssi() {
            return mssi;
        }

        public void setMssi(long mssi) {
            this.mssi = mssi;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(datatypes);
            result = prime * result + dt;
            result = prime * result + (int) (mssi ^ (mssi >>> 32));
            result = prime * result + Arrays.hashCode(waypoints);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj){
                return true;
            }
            if (obj == null){
                return false;
            }
            if (getClass() != obj.getClass()){
                return false;
            }
            SejlRuteRequest other = (SejlRuteRequest) obj;
            if (!Arrays.equals(datatypes, other.datatypes)){
                return false;
            }
            if (dt != other.dt){
                return false;
            }
            if (mssi != other.mssi){
                return false;
            }
            if (!Arrays.equals(waypoints, other.waypoints)){
                return false;
            }
            return true;
        }
        
        
        
    }
    
    
}
