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
package dk.dma.embryo.metoc.json;

import dk.dma.embryo.metoc.json.client.DmiSejlRuteService;
import dk.dma.embryo.metoc.json.client.DmiSejlRuteService.MetocForecast;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

public class Metoc {

    private String created;
    private List<Forecast> forecasts;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result + ((created == null) ? 0 : created.hashCode());
        result = prime * result + ((forecasts == null) ? 0 : forecasts.hashCode());

        return result;
    }

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////
    public static Metoc from(MetocForecast metocForecast) {
        Metoc result = new Metoc();

        try {
            List<Forecast> forecasts = new ArrayList<Forecast>(metocForecast.getForecasts().length);
            for (DmiSejlRuteService.Forecast dmiForecast : metocForecast.getForecasts()) {
                forecasts.add(Forecast.from(dmiForecast));
            }
            result.setCreated(metocForecast.getCreated());
            result.setForecasts(forecasts);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public static Metoc from(MetocForecast metocForecast, Predicate<DmiSejlRuteService.Forecast> predicate) {
        Metoc result = new Metoc();

        try {
            List<Forecast> forecasts = new ArrayList<Forecast>(metocForecast.getForecasts().length);
            for (DmiSejlRuteService.Forecast dmiForecast : metocForecast.getForecasts()) {
                if (predicate.test(dmiForecast)) {
                    forecasts.add(Forecast.from(dmiForecast));
                }
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

    public List<Forecast> getForecasts() {
        return forecasts;
    }
    public void setForecasts(List<Forecast> forecasts) {
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

        public static Forecast from(DmiSejlRuteService.Forecast dmiForecast) throws ParseException {
            Forecast forecast = new Forecast();
            forecast.setLat(dmiForecast.getLat());
            forecast.setLon(dmiForecast.getLon());
            forecast.setTime(DmiSejlRuteService.DATE_FORMAT.parse(dmiForecast.getTime()));
            forecast.setWindDir(dmiForecast.getWindDir() == null ? null : ceil(dmiForecast.getWindDir().getForecast(), 0));
            forecast.setWindSpeed(dmiForecast.getWindSpeed() == null ? null : ceil(dmiForecast.getWindSpeed().getForecast(), 1));
            forecast.setCurDir(dmiForecast.getCurrentDir() == null ? null : ceil(dmiForecast.getCurrentDir().getForecast(), 0));
            forecast.setCurSpeed(dmiForecast.getCurrentSpeed() == null ? null : ceil(dmiForecast.getCurrentSpeed().getForecast(), 1));
            forecast.setWaveDir(dmiForecast.getWaveDir() == null ? null : ceil(dmiForecast.getWaveDir().getForecast(), 0));
            forecast.setWaveHeight(dmiForecast.getWaveHeight() == null ? null : ceil(dmiForecast.getWaveHeight().getForecast(), 1));
            forecast.setWavePeriod(dmiForecast.getWavePeriod() == null ? null : ceil(dmiForecast.getWavePeriod().getForecast(), 2));
            forecast.setSeaLevel(dmiForecast.getSealevel() == null ? null : ceil(dmiForecast.getSealevel().getForecast(), 1));
            return forecast;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((curDir == null) ? 0 : curDir.hashCode());
            result = prime * result + ((curSpeed == null) ? 0 : curSpeed.hashCode());
            long temp;
            temp = Double.doubleToLongBits(lat);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(lon);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            result = prime * result + ((seaLevel == null) ? 0 : seaLevel.hashCode());
            result = prime * result + ((time == null) ? 0 : time.hashCode());
            result = prime * result + ((waveDir == null) ? 0 : waveDir.hashCode());
            result = prime * result + ((waveHeight == null) ? 0 : waveHeight.hashCode());
            result = prime * result + ((wavePeriod == null) ? 0 : wavePeriod.hashCode());
            result = prime * result + ((windDir == null) ? 0 : windDir.hashCode());
            result = prime * result + ((windSpeed == null) ? 0 : windSpeed.hashCode());
            return result;
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
