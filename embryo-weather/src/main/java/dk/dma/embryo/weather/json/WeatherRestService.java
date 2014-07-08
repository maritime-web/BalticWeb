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
package dk.dma.embryo.weather.json;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import dk.dma.embryo.weather.model.Warnings;
import dk.dma.embryo.weather.model.RegionForecast;
import dk.dma.embryo.weather.model.Weather;
import dk.dma.embryo.weather.service.WeatherServiceImpl;

/**
 * 
 * @author Jesper Tejlgaard
 */
@Path("/weather")
public class WeatherRestService {

    @Inject
    private Logger logger;
    
    @Inject
    private WeatherServiceImpl weatherService;

    @GET
    @Path("/warning/{provider}/{region}")
    @Produces("application/json")
    @GZIP
    @NoCache
    public Warnings getWarning(String provider, String region) {
        logger.debug("getWarning({})");

        Warnings warning = weatherService.getWarning();
        
        logger.debug("getWarning({}, {}) : {}", provider, region, warning);
        return warning;
    }

    @GET
    @Path("/forecast/{provider}/{region}")
    @Produces("application/json")
    @GZIP
    @NoCache
    public RegionForecast getForecast(String provider, String region) {
        logger.debug("getForecast({}, {})", provider, region);

        RegionForecast forecast = weatherService.getRegionForecast();

        logger.debug("getForecast({}, {}) : {}", provider, region, forecast);
        return forecast;
    }

    @GET
    @Path("/{provider}/{region}")
    @Produces("application/json")
    @GZIP
    @NoCache
    public Weather getWeather(String provider, String region) {
        logger.debug("getWeather({}, {})", provider, region);

        RegionForecast forecast = weatherService.getRegionForecast();
        Warnings warning = weatherService.getWarning();
        
        Weather weather = new Weather(forecast, warning);

        logger.debug("getWeather({}, {}) : {}", provider, region, weather);
        return weather;
    }
}
