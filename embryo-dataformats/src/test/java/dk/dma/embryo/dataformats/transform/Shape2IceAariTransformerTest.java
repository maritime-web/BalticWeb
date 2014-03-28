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
package dk.dma.embryo.dataformats.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dk.dma.embryo.dataformats.model.IceObservation;
import dk.dma.embryo.dataformats.model.ShapeFileMeasurement;
import dk.dma.embryo.dataformats.transform.Shape2IceAariTransformer;

/**
 * @author Jesper Tejlgaard
 */
public class Shape2IceAariTransformerTest {

    private Shape2IceAariTransformer transformer;

    @Before
    public void setup() {
        Map<String, String> providers = new HashMap<>();
        providers.put("aari", "AARI");

        Map<String, String> regions = new HashMap<>();
        regions.put("gre", "Greenland Sea");
        regions.put("chu", "Chukchi Sea");

        transformer = new Shape2IceAariTransformer(providers, regions);

    }

    @Test
    public void testGetProvider() {
        Assert.assertEquals("aari", transformer.getProvider());
    }

    @Test
    public void testTransform() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd").withZone(DateTimeZone.UTC);
        String dateStr = formatter.print(DateTime.now(DateTimeZone.UTC));

        List<ShapeFileMeasurement> measurements = new ArrayList<>();
        measurements.add(new ShapeFileMeasurement("aari", "aari_gre_" + dateStr + "_pl_a", 10000));
        measurements.add(new ShapeFileMeasurement("aari", "aari_barr_" + dateStr + "_pl_a", 10000));

        List<IceObservation> observations = transformer.transform(measurements);

        Assert.assertNotNull(observations);
        Assert.assertEquals(2, observations.size());

        IceObservation ice = observations.get(0);
        Assert.assertEquals(10000, ice.getSize());
        Assert.assertEquals("Greenland Sea", ice.getRegion());
        Assert.assertEquals("aari.aari_gre_" + dateStr + "_pl_a", ice.getShapeFileName());
        Assert.assertEquals("AARI", ice.getSource());
        Assert.assertNotNull(ice.getDate());
        Assert.assertEquals(formatter.print(DateTime.now(DateTimeZone.UTC)), formatter.print(ice.getDate().getTime()));

        // Must work even though region not mapped
        ice = observations.get(1);
        Assert.assertEquals(10000, ice.getSize());
        Assert.assertEquals("barr", ice.getRegion());
        Assert.assertEquals("aari.aari_barr_" + dateStr + "_pl_a", ice.getShapeFileName());
        Assert.assertEquals("AARI", ice.getSource());
        Assert.assertNotNull(ice.getDate());
        Assert.assertEquals(formatter.print(DateTime.now(DateTimeZone.UTC)), formatter.print(ice.getDate().getTime()));
    }

}
