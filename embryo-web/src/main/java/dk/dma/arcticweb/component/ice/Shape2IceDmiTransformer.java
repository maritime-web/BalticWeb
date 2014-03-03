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
package dk.dma.arcticweb.component.ice;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import dk.dma.embryo.configuration.Property;
import dk.dma.embryo.domain.IceObservation;
import dk.dma.embryo.domain.ShapeFileMeasurement;

/**
 * @author Jesper Tejlgaard
 */
@Named
public class Shape2IceDmiTransformer implements Shape2IceTransformer {

    @Property(value = "embryo.iceChart.providers")
    @Inject
    private Map<String, String> providers;

    @Property(value = "embryo.iceChart.dmi.regions")
    @Inject
    private Map<String, String> regions;
    
    public Shape2IceDmiTransformer() {
        super();
    }

    public Shape2IceDmiTransformer(Map<String, String> providers, Map<String, String> regions) {
        super();
        this.providers = providers;
        this.regions = regions;
    }

    @Override
    public List<IceObservation> transform(List<ShapeFileMeasurement> shapes) {
        List<IceObservation> iceObservations = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMddHHmm").withZone(DateTimeZone.UTC);

        for (ShapeFileMeasurement sfm : shapes) {
            Date date = formatter.parseDateTime(sfm.getFileName().substring(0, 12)).toDate();

            if (System.currentTimeMillis() - date.getTime() < 3600 * 1000L * 24 * 30) {
                String region = sfm.getFileName().substring(13);

                if (regions.containsKey(region)) {
                    region = regions.get(region);
                }

                iceObservations.add(new IceObservation(providers.get(sfm.getProvider()), region, date, sfm
                        .getFileSize(), sfm.getProvider() + "." + sfm.getFileName()));
            }
        }

        return iceObservations;
    }

    @Override
    public String getProvider() {
        return "dmi";
    }
}
