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

import java.util.HashMap;
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

        ShapeFileMeasurement shapeGreenland = new ShapeFileMeasurement("iceChart", "aari", "aari_gre_" + dateStr + "_pl_a", 10000);
        ShapeFileMeasurement shapeBar = new ShapeFileMeasurement("iceChart", "aari", "aari_barr_" + dateStr + "_pl_a", 10000);

        IceObservation ice = transformer.transform(shapeGreenland);

        Assert.assertNotNull(ice);
        Assert.assertEquals(10000, ice.getSize());
        Assert.assertEquals("Greenland Sea", ice.getRegion());
        Assert.assertEquals("iceChart-aari.aari_gre_" + dateStr + "_pl_a", ice.getShapeFileName());
        Assert.assertEquals("AARI", ice.getSource());
        Assert.assertNotNull(ice.getDate());
        Assert.assertEquals(formatter.print(DateTime.now(DateTimeZone.UTC)), formatter.print(ice.getDate().getTime()));

        // Must work even though region not mapped
        ice = transformer.transform(shapeBar);
        Assert.assertNotNull(ice);
        Assert.assertEquals(10000, ice.getSize());
        Assert.assertEquals("barr", ice.getRegion());
        Assert.assertEquals("iceChart-aari.aari_barr_" + dateStr + "_pl_a", ice.getShapeFileName());
        Assert.assertEquals("AARI", ice.getSource());
        Assert.assertNotNull(ice.getDate());
        Assert.assertEquals(formatter.print(DateTime.now(DateTimeZone.UTC)), formatter.print(ice.getDate().getTime()));
    }

}
