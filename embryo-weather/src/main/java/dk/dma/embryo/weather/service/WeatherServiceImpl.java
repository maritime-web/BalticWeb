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
package dk.dma.embryo.weather.service;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.weather.model.GaleWarning;
import dk.dma.embryo.weather.model.RegionForecast;

/**
 * 
 * @author Jesper Tejlgaard
 */
@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class WeatherServiceImpl {

    @Inject
    @Property(value = "embryo.weather.dmi.localDirectory", substituteSystemProperties = true)
    private String localDmiDir;

    private RegionForecast forecast;
    private GaleWarning warning;
    
    @Inject
    private DmiForecastParser_En parser;

    @Inject
    private Logger logger;

    public WeatherServiceImpl() {
    }

    @Lock(LockType.READ)
    public RegionForecast getRegionForecast() {
        return forecast;
    }

    @Lock(LockType.READ)
    public GaleWarning getWarning() {
        return warning;
    }
    
    @Lock(LockType.WRITE)
    public void setValues(RegionForecast forecast, GaleWarning warning){
        this.forecast = forecast;
        this.warning = warning;
    }

    @PostConstruct
    public void init(){
        try {
            refresh();
        }catch (Exception e){
            logger.error("Error initializing {}", getClass().getSimpleName(), e);
        }
    }
    
    public void refresh() throws IOException {
        RegionForecast fResult = readForecasts();
        GaleWarning wResult = readGaleWarnings();
        setValues(fResult, wResult);
    }

    private RegionForecast readForecasts() throws IOException {
        String fn = localDmiDir + "/grudseng.xml";
        return parser.parse(new File(fn));
    }

    private GaleWarning readGaleWarnings() throws IOException {
        String fn = localDmiDir + "/gronvar.xml";
        DmiGaleWarningParser parser = new DmiGaleWarningParser(new File(fn));
        return parser.parse();
    }
}
