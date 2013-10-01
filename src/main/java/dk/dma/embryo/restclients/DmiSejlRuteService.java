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
package dk.dma.embryo.restclients;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.io.IOException;
import java.text.SimpleDateFormat;

public interface DmiSejlRuteService {
    SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.000Z");

    @POST()
    @Path("/SR")
    SejlRuteResponse sejlRute(@FormParam("req") SejlRuteRequest request);

    public static class WindDir {
        private double forecast;

        public double getForecast() {
            return forecast;
        }

        public void setForecast(double forecast) {
            this.forecast = forecast;
        }
    }

    public static class WindSpeed {
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
        private WindDir windDir;
        @JsonProperty("wind-speed")
        private WindSpeed windSpeed;

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

        public WindDir getWindDir() {
            return windDir;
        }

        public void setWindDir(WindDir windDir) {
            this.windDir = windDir;
        }

        public WindSpeed getWindSpeed() {
            return windSpeed;
        }

        public void setWindSpeed(WindSpeed windSpeed) {
            this.windSpeed = windSpeed;
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
    }

    public static class SejlRuteRequest {
        private long mssi;
        private String[] datatypes;
        private int dt;
        private Waypoint[] waypoints;

        public String toString() {
            return RestClientFactory.asJson(this);
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
    }
}
