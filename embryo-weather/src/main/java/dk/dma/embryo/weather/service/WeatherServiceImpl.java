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

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.weather.model.RegionForecast;
import dk.dma.embryo.weather.model.Warnings;

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
    
    public static final String FORECAST_FILENAME = "grudseng";
    public static final String GALE_WARNING_FILENAME = "gronvar";

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
    public void setValues(RegionForecast forecast, Warnings warning) {
        this.forecast = forecast;
        this.warning = warning;
    }

    @PostConstruct
    public void init() {
        try {
            refresh();
        } catch (Exception e) {
            logger.error("Error initializing {}", getClass().getSimpleName(), e);
        }
    }

    public void refresh() throws IOException {
        RegionForecast fResult = readForecasts();
        Warnings wResult = readGaleWarnings();
        setValues(fResult, wResult);
    }

    private RegionForecast readForecasts() throws IOException {
        File file = getXmlFileName(FORECAST_FILENAME);
        try {
            return parser.parse(file);
        } catch (RuntimeException | IOException e) {
            saveFaultyFile(FORECAST_FILENAME);
            throw e;
        }
    }

    private Warnings readGaleWarnings() throws IOException {
        File file = getXmlFileName(GALE_WARNING_FILENAME);
        try {
            DmiWarningParser parser = new DmiWarningParser(file);
            WarningTranslator translator = new WarningTranslator();
            return translator.fromDanishToEnglish(parser.parse());
        } catch (RuntimeException | IOException e) {
            saveFaultyFile(GALE_WARNING_FILENAME);
            throw e;
        }
    }

    private void saveFaultyFile(String filename) {
        File faultyDir = new File(localDmiDir + "/faulty");
        if(!faultyDir.exists()) {
            faultyDir.mkdir();
        }
        File sourceFile = getXmlFileName(filename);
        DateTime now = DateTime.now(DateTimeZone.UTC);
        String dateStr = now.toString("_yyyy_MM_dd-HH_mm_ss");
        File destFile = new File(faultyDir.getPath() + "/" + filename + dateStr + ".xml");
        try {
            FileUtils.copyFile(sourceFile, destFile);
        } catch (IOException e) {
            logger.error("Could not copy faulty file {} to {}", sourceFile.getAbsolutePath(), destFile.getAbsolutePath(), e);
        }
    }
    
    private File getXmlFileName(String filename) {
        return new File(localDmiDir + "/" + filename + ".xml");
    }
}
