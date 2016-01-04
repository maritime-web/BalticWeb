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

import dk.dma.embryo.common.configuration.LogConfiguration;
import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.weather.model.DistrictForecast;
import dk.dma.embryo.weather.model.RegionForecast;
import dk.dma.embryo.weather.model.Warnings;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.unitils.reflectionassert.ReflectionAssert;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jesper Tejlgaard
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({ PropertyFileService.class, LogConfiguration.class })
public class DmiForecastParser_EnTest {

    @Inject
    DmiForecastParser_En parser;
    
    @Test
    public void test() throws IOException {

        List<DistrictForecast> expected = new ArrayList<>();
        expected.add(new DistrictForecast(
                "Daneborg",
                "North and northeast, 3 to 8 m/s, from this evening in western part variable, below 6 m/s. Mainly good visibility.",
                "Significant wave height: 3 m. Swells: 3 m.",
                "Old ice and thin first-year ice in the western and central part. Few icebergs and growlers."));
        expected.add(new DistrictForecast(
                "Kangikajik",
                "Gradually north and northwest, increasing up to gale 10 to 18 m/s, Tuesday gradually cyclonic variable, up to gale about 15 m/s, in southernmost part mainly southwest and south, 5 to 10 m/s. Rain and moderate to poor visibility.",
                "Significant wave height: 3,5 m. Swells: 3 m."));
        expected.add(new DistrictForecast(
                "Aputiteeq",
                "North and northeast, 5 to 10 m/s, this evening decreasing and this night variable, below 8 m/s, Tuesday southwest becoming south , 3 to 8 m/s. In northern part locally rain with moderate visibility, otherwise good visibility, apart from fog patches.",
                "Significant wave height: 1,5 m. Swells: 2 m."));

        InputStream is = getClass().getResourceAsStream("/dmi/grudseng-mini.xml");

        RegionForecast forecast = parser.parse(is);

        String expectedOverview = "A low, 1005 hPa, northeast of Iceland, is moving towards westnorthwest to Kangikajik, gradually filling some. A low, 9805 hPa, over Labrador Sea, is moving slowly towards north.";

        Assert.assertEquals(expectedOverview, forecast.getDesc());
        Assert.assertEquals("15.00 UTC", forecast.getTime());
        Assert.assertNotNull(forecast.getFrom());
        Assert.assertEquals(1404755100000L, forecast.getFrom().getTime());
        Assert.assertNotNull(forecast.getTo());
        Assert.assertEquals(1404842400000L, forecast.getTo().getTime());
        Assert.assertEquals(3, forecast.getDistricts().size());

        ReflectionAssert.assertReflectionEquals(expected, forecast.getDistricts());
    }

    @Test
    public void testYearChange() throws IOException {

        List<DistrictForecast> expected = new ArrayList<>();
        expected.add(new DistrictForecast(
                "Daneborg",
                "North and northeast, 8 to 13 m/s, this night in western up to gale, 15 m/s, Tuesday east and northeast, 8 to 13 m/s, becoming southeast and decreasing to 5 to 10 m/s. Locally fog and transiently rain from east, mainly moderate to poor visibility, Tuesday gradually some improving visibility from southeast.",
                "Significant wave height: 3 m. Swells: 2,5 m."));

        InputStream is = getClass().getResourceAsStream("/dmi/grudseng-yearchange.xml");

        RegionForecast forecast = parser.parse(is);

        Assert.assertNotNull(forecast.getFrom());
        Assert.assertEquals(1388473200000L, forecast.getFrom().getTime());
        Assert.assertNotNull(forecast.getTo());
        Assert.assertEquals(1388556000000L, forecast.getTo().getTime());
        Assert.assertEquals(1, forecast.getDistricts().size());

        ReflectionAssert.assertReflectionEquals(expected, forecast.getDistricts());
    }

    @Test
    public void testFullFile() throws IOException {

        InputStream is = getClass().getResourceAsStream("/dmi/grudseng.xml");

        RegionForecast forecast = parser.parse(is);

        String expectedOverview = "A low, 1005 hPa, northeast of Iceland, is moving towards westnorthwest to Kangikajik, gradually filling some. A low, 9805 hPa, over Labrador Sea, is moving slowly towards north.";
        Assert.assertEquals(expectedOverview, forecast.getDesc());
        Assert.assertEquals("15.00 UTC", forecast.getTime());
        Assert.assertNotNull(forecast.getFrom());
        Assert.assertEquals(1404755100000L, forecast.getFrom().getTime());
        Assert.assertNotNull(forecast.getTo());
        Assert.assertEquals(1404842400000L, forecast.getTo().getTime());
        Assert.assertEquals(14, forecast.getDistricts().size());
    }

    @Test
    public void testDifferentTimeFormat() throws IOException {

        InputStream is = getClass().getResourceAsStream("/dmi/grudseng-03-08-2014--14-28-08-differentTimeFormat.xml");

        RegionForecast forecast = parser.parse(is);

        String expectedOverview = "A minor low, 1010 hPa, over Uiffaq is nearly stationary while filling. A low, 990 hPa, east of Cape Farewell, is nearly stationary. A high, 1021 hPa, over the southern and centrale Greenland, weakens gradually.";
        Assert.assertEquals(expectedOverview, forecast.getDesc());
        Assert.assertEquals("/1200 UTC", forecast.getTime());
        Assert.assertNotNull(forecast.getFrom());
        Assert.assertEquals(1407068400000L, forecast.getFrom().getTime());
        Assert.assertNotNull(forecast.getTo());
        Assert.assertEquals(1407153600000L, forecast.getTo().getTime());
        Assert.assertEquals(14, forecast.getDistricts().size());
    }
    
    @Test
    public void test20141209FailedInProduction() throws IOException {
        InputStream is = getClass().getResourceAsStream("/dmi/grudseng-2014-12-09.xml");

        RegionForecast forecast = parser.parse(is);

        String expectedOverview = "An area with low pressure, 940 hPa, a little northeast of " +
                "Timmiarmiut is moving towards east, and another low 960 hPa " +
                "over it northwestern Iceland is moving towards northeast, and " +
                "gives up to hurricane along østkysten. A minor low, 995 hPa, " +
                "over Melvillebugten is filling. Smaller low developes this " +
                "evening over Davis Strait is movung towards southsoutheast.";
        Assert.assertEquals(expectedOverview, forecast.getDesc());
        Assert.assertEquals("/0600 UTC.", forecast.getTime());
        Assert.assertNotNull(forecast.getFrom());
        Assert.assertEquals(1418113800000L, forecast.getFrom().getTime());
        Assert.assertNotNull(forecast.getTo());
        Assert.assertEquals(1418191200000L, forecast.getTo().getTime());
        Assert.assertEquals(14, forecast.getDistricts().size());

    }

    @Test
    public void test201508061600FailedInProduction() throws IOException {
        InputStream is = getClass().getResourceAsStream("/dmi/grudseng_2015_08_05-08_16_00.xml");
        try {
            RegionForecast forecast = parser.parse(is);
            Assert.fail("Parser error expected");
        } catch (Exception e) {
            // Should fail because of bad date format
            Assert.assertTrue(e.getClass() == IOException.class);
            Assert.assertEquals("Error parsing weather forecast", e.getMessage());
            Assert.assertTrue(e.getCause().getClass() == IllegalArgumentException.class);
            Assert.assertEquals("Invalid format: \"thursday d 6 August 2015 06\" is malformed at \"d 6 August 2015 06\"", e.getCause().getMessage());
        }
    }

    @Test
    public void testGrudseng() throws IOException {
        InputStream is = getClass().getResourceAsStream("/dmi/grudsengxml-14-09-2014--00-23-21.xml");
        RegionForecast forecast = parser.parse(is);
    }
    
    @Test
    public void testGronvar() throws IOException {
        InputStream is = getClass().getResourceAsStream("/dmi/gronvarxml-14-09-2014--00-19-54.xml");
        DmiWarningParser warningParser = new DmiWarningParser(is);
        Warnings warnings = new WarningTranslator().fromDanishToEnglish(warningParser.parse());
    }

    @Test
    public void testGrudseng20151124() throws IOException {
        InputStream is = getClass().getResourceAsStream("/dmi/grudseng_2015_11_24-10_28_00.xml");
        RegionForecast forecast = parser.parse(is);
    }


}
