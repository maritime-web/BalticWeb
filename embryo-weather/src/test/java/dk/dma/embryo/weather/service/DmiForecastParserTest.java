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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;

import dk.dma.embryo.weather.model.DistrictForecast;
import dk.dma.embryo.weather.model.RegionForecast;

/**
 * @author Jesper Tejlgaard
 */
public class DmiForecastParserTest {

    @Test
    public void test() throws IOException {

        List<DistrictForecast> expected = new ArrayList<>();
        expected.add(new DistrictForecast("Daneborg",
                "Syd og sydøst, 3 til 8 m/s, ud på natten 5 til 10 m/s. Stedvis tåge, ellers mest god sigt.",
                "Signifikant bølgehøjde: 1,5 m. Dønning: 1,5 m."));
        expected.add(new DistrictForecast(
                "Kangikajik",
                "I den sydlige del endnu skiftende, under 6 m/s. Ellers sydøst, 3 til 8 m/s, til formiddag drejende syd og sydvest og i aften 5 til 10 m/s. I den sydlige del endnu stedvis regn, ellers mest god sigt, dog risiko for tåge.",
                "Signifikant bølgehøjde: 1,5 m. Dønning: 1,5 m."));
        expected.add(new DistrictForecast("Aputiteeq",
                "Nordøst eller skiftende, under 8 m/s. Stedvis regn, moderat til god sigt, dog risiko for tåge.",
                "Signifikant bølgehøjde: 1,5 m. Dønning: 2 m."));

        InputStream is = getClass().getResourceAsStream("/dmi/gruds-mini.xml");

        DmiForecastParser parser = new DmiForecastParser(is);
        RegionForecast forecast = parser.parse();

        String expectedOverview = "Et højtryk, 1025 hPa, over Norskehavet ligger stille. Et lavtryk, 1010 hPa, øst "
                + "for Kap Farvel fyldes op. Et lavtryk, ca 995 hPa, over Baffin bugt fyldes kun langsomt op og tilhørende"
                + " fronter giver endnu regn og blæst til store dele af Vestgrønland. Et lavtryk, ca 1010 hPa, bevæger sig "
                + "i løbet af dagen til syd for Kap Farvel sydvestfra og derfra lidt videre mod nordøst.";

        Assert.assertEquals(expectedOverview, forecast.getDesc());
        Assert.assertEquals("00 utc", forecast.getTime());
        Assert.assertNotNull(forecast.getFrom());
        Assert.assertEquals(1403679600000L, forecast.getFrom().getTime());
        Assert.assertNotNull(forecast.getTo());
        Assert.assertEquals(1403762400000L, forecast.getTo().getTime());
        Assert.assertEquals(3, forecast.getDistricts().size());

        ReflectionAssert.assertReflectionEquals(expected, forecast.getDistricts());
    }

    @Test
    public void testYearChange() throws IOException {

        List<DistrictForecast> expected = new ArrayList<>();
        expected.add(new DistrictForecast("Daneborg",
                "Syd og sydøst, 3 til 8 m/s, ud på natten 5 til 10 m/s. Stedvis tåge, ellers mest god sigt.",
                "Signifikant bølgehøjde: 1,5 m. Dønning: 1,5 m."));

        InputStream is = getClass().getResourceAsStream("/dmi/gruds-yearchange.xml");

        DmiForecastParser parser = new DmiForecastParser(is);
        RegionForecast forecast = parser.parse();

        Assert.assertNotNull(forecast.getFrom());
        Assert.assertEquals(1388473200000L, forecast.getFrom().getTime());
        Assert.assertNotNull(forecast.getTo());
        Assert.assertEquals(1388556000000L, forecast.getTo().getTime());
        Assert.assertEquals(1, forecast.getDistricts().size());

        ReflectionAssert.assertReflectionEquals(expected, forecast.getDistricts());
    }
}
