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
package dk.dma.embryo.metoc.json;

import java.text.ParseException;
import java.util.Date;

import dk.dma.embryo.metoc.json.client.DmiSejlRuteService;
import dk.dma.embryo.metoc.json.client.DmiSejlRuteService.MetocForecast;

public class Metoc {

    private String created;
    private Forecast[] forecasts;

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////
    public static Metoc from(MetocForecast metocForecast) {
        Metoc result = new Metoc();

        try {
            Forecast[] forecasts = new Forecast[metocForecast.getForecasts().length];
            int count = 0;
            for (DmiSejlRuteService.Forecast dmiForecast : metocForecast.getForecasts()) {
                forecasts[count++] = Forecast.from(dmiForecast);
            }
            result.setCreated(metocForecast.getCreated());
            result.setForecasts(forecasts);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

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
        private Date time;
        private Double windDir;
        private Double windSpeed;
        private Double curDir;
        private Double curSpeed;
        private Double waveDir;
        private Double waveHeight;
        private Double wavePeriod;
        private Double seaLevel;

        public static Forecast from(DmiSejlRuteService.Forecast dmiForecast)
                throws ParseException {
            Forecast forecast = new Forecast();
            forecast.setLat(dmiForecast.getLat());
            forecast.setLon(dmiForecast.getLon());
            forecast.setTime(DmiSejlRuteService.DATE_FORMAT.parse(dmiForecast.getTime()));
            forecast.setWindDir(dmiForecast.getWindDir() == null ? null : ceil(dmiForecast.getWindDir().getForecast(),
                    0));
            forecast.setWindSpeed(dmiForecast.getWindSpeed() == null ? null : ceil(dmiForecast.getWindSpeed()
                    .getForecast(), 1));
            forecast.setCurDir(dmiForecast.getCurrentDir() == null ? null : ceil(dmiForecast.getCurrentDir()
                    .getForecast(), 0));
            forecast.setCurSpeed(dmiForecast.getCurrentSpeed() == null ? null : ceil(dmiForecast.getCurrentSpeed()
                    .getForecast(), 1));
            forecast.setWaveDir(dmiForecast.getWaveDir() == null ? null : ceil(dmiForecast.getWaveDir().getForecast(),
                    0));
            forecast.setWaveHeight(dmiForecast.getWaveHeight() == null ? null : ceil(dmiForecast.getWaveHeight()
                    .getForecast(), 1));
            forecast.setWavePeriod(dmiForecast.getWavePeriod() == null ? null : ceil(dmiForecast.getWavePeriod()
                    .getForecast(), 2));
            forecast.setSeaLevel(dmiForecast.getSealevel() == null ? null : ceil(dmiForecast.getSealevel()
                    .getForecast(), 1));
            return forecast;
        }

        private static double ceil(double number, int decimals) {
            double d = Math.pow(10.0, decimals);
            return Math.ceil(number * d) / d;
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

        public Date getTime() {
            return time;
        }

        public void setTime(Date time) {
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
