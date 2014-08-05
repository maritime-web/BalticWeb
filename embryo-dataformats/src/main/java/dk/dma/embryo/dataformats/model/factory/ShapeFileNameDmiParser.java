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
package dk.dma.embryo.dataformats.model.factory;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.dataformats.model.ShapeFileMeasurement;

/**
 * @author Jesper Tejlgaard
 */
@Named
public class ShapeFileNameDmiParser implements ShapeFileNameParser {

    private Map<String, String> regions;
    
    @Inject
    private PropertyFileService propertyFileService;
    
    public ShapeFileNameDmiParser() {
    }

    public ShapeFileNameDmiParser(Map<String, String> regions) {
        super();
        this.regions = regions;
    }

    @Override
    public ShapeFileMeasurement parse(String chartType, String name) {
        if(regions == null) {
            regions = propertyFileService.getMapProperty("embryo." + chartType + ".dmi.regions");
        }
        
        String fileName = name; 
        int version = 0;
        
        for(String region : regions.keySet()){
            int i = name.indexOf(region);
            if(i >= 0){
                fileName = name.substring(0, i + region.length());
                if(name.length() > i + region.length()){
                    version = Integer.parseInt(name.substring(i + region.length() + 2));
                }
            }
        }

        ShapeFileMeasurement measurement = new ShapeFileMeasurement();
        measurement.setFileName(fileName);
        measurement.setVersion(version);
        measurement.setProvider(getProvider());
        measurement.setChartType(chartType);
        return measurement;
    }

    @Override
    public String getProvider() {
        return "dmi";
    }
}
