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

import dk.dma.embryo.domain.IceObservation;
import dk.dma.embryo.domain.ShapeFileMeasurement;

/**
 * @author Jesper Tejlgaard
 */
public class Shape2IceDmiTransformerTest {

    private Shape2IceDmiTransformer transformer;

    @Before
    public void setup() {
        Map<String, String> providers = new HashMap<>();
        providers.put("dmi", "DMI");

        Map<String, String> regions = new HashMap<>();
        regions.put("CapeFarewell_RIC", "Cape Farewell");
        regions.put("Greenland_WA", "Greenland Overview");
        
        transformer = new Shape2IceDmiTransformer(providers, regions);
    }

    @Test
    public void testGetProvider() {
        Assert.assertEquals("dmi", transformer.getProvider());
    }

    @Test
    public void testTransform() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMddHHmm").withZone(DateTimeZone.UTC);
        String dateStr = formatter.print(DateTime.now(DateTimeZone.UTC));

        List<ShapeFileMeasurement> measurements = new ArrayList<>();
        measurements.add(new ShapeFileMeasurement("dmi", dateStr + "_CapeFarewell_RIC", 20000));
        measurements.add(new ShapeFileMeasurement("dmi", dateStr + "_Greenland_WA", 30000));

        List<IceObservation> observations = transformer.transform(measurements);

        Assert.assertNotNull(observations);
        Assert.assertEquals(2, observations.size());

        IceObservation ice = observations.get(0);
        Assert.assertEquals(20000, ice.getSize());
        Assert.assertEquals("Cape Farewell", ice.getRegion());
        Assert.assertEquals("dmi." + dateStr + "_CapeFarewell_RIC", ice.getShapeFileName());
        Assert.assertEquals("DMI", ice.getSource());
        Assert.assertNotNull(ice.getDate());
        Assert.assertEquals(formatter.print(DateTime.now(DateTimeZone.UTC)), formatter.print(ice.getDate().getTime()));

        // Must work even though region not mapped
        ice = observations.get(1);
        Assert.assertEquals(30000, ice.getSize());
        Assert.assertEquals("Greenland Overview", ice.getRegion());
        Assert.assertEquals("dmi." + dateStr + "_Greenland_WA", ice.getShapeFileName());
        Assert.assertEquals("DMI", ice.getSource());
        Assert.assertNotNull(ice.getDate());
        Assert.assertEquals(formatter.print(DateTime.now(DateTimeZone.UTC)), formatter.print(ice.getDate().getTime()));
    }

}
