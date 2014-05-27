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
