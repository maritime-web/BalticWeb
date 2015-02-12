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
package dk.dma.embryo.dataformats.transform;

import java.util.Date;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.dataformats.model.IceObservation;
import dk.dma.embryo.dataformats.model.ShapeFileMeasurement;

/**
 * @author Jesper Tejlgaard
 */
@Named
public class Shape2IceDmiTransformer implements Shape2IceTransformer {

    @Property(value = "embryo.iceChart.providers")
    @Inject
    private Map<String, String> providers;

    private Map<String, String> regions;

    @Inject
    private PropertyFileService propertyFileService;

    private DateTimeFormatter longFormatter = DateTimeFormat.forPattern("yyyyMMddHHmm").withZone(DateTimeZone.UTC);
    private DateTimeFormatter shortFormatter = DateTimeFormat.forPattern("yyyyMMdd").withZone(DateTimeZone.UTC);

    public Shape2IceDmiTransformer() {
        super();
    }

    public Shape2IceDmiTransformer(Map<String, String> providers, Map<String, String> regions) {
        super();
        this.providers = providers;
        this.regions = regions;
    }

    @Override
    public IceObservation transform(ShapeFileMeasurement shape) {
        if (regions == null) {
            regions = propertyFileService.getMapProperty("embryo." + shape.getChartType() + ".dmi.regions");
        }

        Date date;
        String fileName = shape.getFileName();
        if (fileName.indexOf("_") == 8) {
            date = shortFormatter.parseDateTime(fileName.substring(0, 8)).toDate();
        } else {
            date = longFormatter.parseDateTime(fileName.substring(0, 12)).toDate();
        }

        String region = fileName.substring(13);
        if (regions.containsKey(region)) {
            region = regions.get(region);
        }

        return new IceObservation(providers.get(shape.getProvider()), region, date, shape.getFileSize(), shape.getChartType() + "-"
                + shape.getProvider() + "." + fileName);
    }

    @Override
    public String getProvider() {
        return "dmi";
    }
}
