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
package dk.dma.embryo.rest.json;

import dk.dma.embryo.restclients.DmiSejlRuteService.MetocForecast;



public class Metoc {
    
    private String created;
    private Forecast[] forecasts;

    
    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////
    public static Metoc from(MetocForecast metocForecast){
        Metoc result = new Metoc();
        
        Forecast[] forecasts = new Forecast[metocForecast.getForecasts().length];
        int count = 0;
        for(dk.dma.embryo.restclients.DmiSejlRuteService.Forecast dmiForecast : metocForecast.getForecasts()){
            forecasts[count++] = Forecast.from(dmiForecast);
        }
        result.setCreated(metocForecast.getCreated());
        result.setForecasts(forecasts);
        
        return result;
    }
    
    
    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
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

    // //////////////////////////////////////////////////////////////////////
    // Inner classes
    // //////////////////////////////////////////////////////////////////////
    public static class Forecast {
        private double lat;
        private double lon;
        private String time;
        private Double windDir;
        private Double windSpeed;
        private Double curDir;
        private Double curSpeed;
        private Double waveDir;
        private Double waveHeight;
        private Double wavePeriod;
        private Double seaLevel;
        
        
        public static Forecast from(dk.dma.embryo.restclients.DmiSejlRuteService.Forecast dmiForecast){
            Forecast forecast = new Forecast();
            forecast.setLat(dmiForecast.getLat());
            forecast.setLon(dmiForecast.getLon());
            forecast.setTime(dmiForecast.getTime());
            forecast.setWindDir(dmiForecast.getWindDir() == null ? null : dmiForecast.getWindDir().getForecast());
            forecast.setWindSpeed(dmiForecast.getWindSpeed() == null ? null : dmiForecast.getWindSpeed().getForecast());
            forecast.setCurDir(dmiForecast.getCurrentDir() == null ? null : dmiForecast.getCurrentDir().getForecast());
            forecast.setCurSpeed(dmiForecast.getCurrentSpeed() == null ? null : dmiForecast.getCurrentSpeed().getForecast());
            forecast.setWaveDir(dmiForecast.getWaveDir() == null ? null : dmiForecast.getWaveDir().getForecast());
            forecast.setWaveHeight(dmiForecast.getWaveHeight() == null ? null : dmiForecast.getWaveHeight().getForecast());
            forecast.setWavePeriod(dmiForecast.getWavePeriod() == null ? null : dmiForecast.getWavePeriod().getForecast());
            forecast.setSeaLevel(dmiForecast.getSealevel() == null ? null : dmiForecast.getSealevel().getForecast());
            return forecast;
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

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public Double getWindDir() {
            return windDir;
        }

        public void setWindDir(Double windDir) {
            this.windDir = windDir;
        }

        public Double getWindSpeed() {
            return windSpeed;
        }

        public void setWindSpeed(Double windSpeed) {
            this.windSpeed = windSpeed;
        }

        public Double getCurDir() {
            return curDir;
        }

        public void setCurDir(Double curDir) {
            this.curDir = curDir;
        }

        public Double getCurSpeed() {
            return curSpeed;
        }

        public void setCurSpeed(Double curSpeed) {
            this.curSpeed = curSpeed;
        }

        public Double getWaveDir() {
            return waveDir;
        }

        public void setWaveDir(Double waveDir) {
            this.waveDir = waveDir;
        }

        public Double getWaveHeight() {
            return waveHeight;
        }

        public void setWaveHeight(Double waveHeight) {
            this.waveHeight = waveHeight;
        }

        public Double getWavePeriod() {
            return wavePeriod;
        }

        public void setWavePeriod(Double wavePeriod) {
            this.wavePeriod = wavePeriod;
        }

        public Double getSeaLevel() {
            return seaLevel;
        }

        public void setSeaLevel(Double seaLevel) {
            this.seaLevel = seaLevel;
        }
    }
}
