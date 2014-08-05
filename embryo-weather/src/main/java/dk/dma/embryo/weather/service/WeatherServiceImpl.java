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
import dk.dma.embryo.weather.model.Warnings;
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
    private Warnings warning;
    
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
    public Warnings getWarning() {
        return warning;
    }
    
    @Lock(LockType.WRITE)
    public void setValues(RegionForecast forecast, Warnings warning){
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
        Warnings wResult = readGaleWarnings();
        setValues(fResult, wResult);
    }

    private RegionForecast readForecasts() throws IOException {
        String fn = localDmiDir + "/grudseng.xml";
        return parser.parse(new File(fn));
    }

    private Warnings readGaleWarnings() throws IOException {
        String fn = localDmiDir + "/gronvar.xml";
        DmiWarningParser parser = new DmiWarningParser(new File(fn));
        WarningTranslator translator = new WarningTranslator();
        return translator.fromDanishToEnglish(parser.parse());
    }
}
