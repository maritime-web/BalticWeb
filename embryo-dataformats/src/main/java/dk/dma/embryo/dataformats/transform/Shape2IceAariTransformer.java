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
import dk.dma.embryo.dataformats.model.IceObservation;
import dk.dma.embryo.dataformats.model.ShapeFileMeasurement;

/**
 * @author Jesper Tejlgaard
 */
@Named
public class Shape2IceAariTransformer implements Shape2IceTransformer {

    @Property(value = "embryo.iceChart.providers")
    @Inject
    private Map<String, String> providers;

    @Property(value = "embryo.iceChart.aari.regions")
    @Inject
    private Map<String, String> regions;

    public Shape2IceAariTransformer() {
    }

    public Shape2IceAariTransformer(Map<String, String> providers, Map<String, String> regions) {
        this.providers = providers;
        this.regions = regions;
    }
    
    @Override
    public List<IceObservation> transform(String chartType, List<ShapeFileMeasurement> shapes) {
        List<IceObservation> iceObservations = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd").withZone(DateTimeZone.UTC);

        for (ShapeFileMeasurement sfm : shapes) {
            String[] fileNameParts = sfm.getFileName().split("_");

            Date date = formatter.parseDateTime(fileNameParts[2]).toDate();

            if (System.currentTimeMillis() - date.getTime() < 3600 * 1000L * 24 * 30) {
                String region = fileNameParts[1];

                if (regions.containsKey(region)) {
                    region = regions.get(region);
                }

                iceObservations.add(new IceObservation(providers.get(sfm.getProvider()), region, date, sfm
                        .getFileSize(), sfm.getChartType() + "-" + sfm.getProvider() + "." + sfm.getFileName()));
            }
        }

        return iceObservations;

    }

    @Override
    public String getProvider() {
        return "aari";
    }

}
