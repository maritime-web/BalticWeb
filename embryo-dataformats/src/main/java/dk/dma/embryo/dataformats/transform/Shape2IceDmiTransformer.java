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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    public Shape2IceDmiTransformer() {
        super();
    }

    public Shape2IceDmiTransformer(Map<String, String> providers, Map<String, String> regions) {
        super();
        this.providers = providers;
        this.regions = regions;
    }

    @Override
    public List<IceObservation> transform(String chartType, List<ShapeFileMeasurement> shapes) {
        if (regions == null) {
            regions = propertyFileService.getMapProperty("embryo." + chartType + ".dmi.regions");
        }
        List<IceObservation> iceObservations = new ArrayList<>();

        DateTimeFormatter longFormatter = DateTimeFormat.forPattern("yyyyMMddHHmm").withZone(DateTimeZone.UTC);
        DateTimeFormatter shortFormatter = DateTimeFormat.forPattern("yyyyMMdd").withZone(DateTimeZone.UTC);

        for (ShapeFileMeasurement sfm : shapes) {
            Date date = null;
            String fileName = sfm.getFileName();
            if (fileName.indexOf("_") == 8) {
                date = shortFormatter.parseDateTime(fileName.substring(0, 8)).toDate();
            } else {
                date = longFormatter.parseDateTime(fileName.substring(0, 12)).toDate();
            }

            if (System.currentTimeMillis() - date.getTime() < 3600 * 1000L * 24 * 30) {
                String region = fileName.substring(13);

                if (regions.containsKey(region)) {
                    region = regions.get(region);
                }

                iceObservations.add(new IceObservation(providers.get(sfm.getProvider()), region, date, sfm.getFileSize(), sfm.getChartType() + "-"
                        + sfm.getProvider() + "." + fileName));
            }
        }

        return iceObservations;
    }

    @Override
    public String getProvider() {
        return "dmi";
    }
}
