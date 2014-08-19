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
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import dk.dma.embryo.weather.model.Warnings;

/**
 * @author Jesper Tejlgaard
 */
public class DmiGaleWarningParserTest {

    @Test
    public void test() throws IOException {

        Map<String, String> expected = new HashMap<>();
        expected.put("Nunarsuit", "Sydøst 18 m/s.");
        expected.put("Narsalik", "Sydøst 23 m/s.");
        expected.put("Meqquitsoq", "Sydsydøst 23 m/s.");
        expected.put("Attu", "Sydøst 20 m/s.");

        InputStream is = getClass().getResourceAsStream("/dmi/gronvar.xml");

        DmiWarningParser parser = new DmiWarningParser(is);
        Warnings warning = parser.parse();

        Assert.assertNotNull(warning.getFrom());
        Assert.assertEquals(1403679360000L, warning.getFrom().getTime());
        Assert.assertEquals(Integer.valueOf(614), warning.getNumber());
        Assert.assertEquals(expected, warning.getGale());
    }

    @Test
    public void testWithAllWarningTypes() throws IOException {

        Map<String, String> expectedGaleWarnings = new HashMap<>();
        expectedGaleWarnings.put("Nunarsuit", "Sydøst 18 m/s.");
        expectedGaleWarnings.put("Narsalik", "Sydøst 23 m/s.");
        expectedGaleWarnings.put("Meqquitsoq", "Sydsydøst 23 m/s.");
        expectedGaleWarnings.put("Attu", "Sydøst 20 m/s.");

        Map<String, String> expectedStormWarnings = new HashMap<>();
        expectedStormWarnings.put("Nunarsuit", "Sydøst 10 m/s.");
        expectedStormWarnings.put("Narsalik", "Sydøst 15.");

        Map<String, String> expectedIceWarnings = new HashMap<>();
        expectedIceWarnings.put("Attu", "Hep hey");
        
        InputStream is = getClass().getResourceAsStream("/dmi/gronvar-all-selfinvented.xml");

        DmiWarningParser parser = new DmiWarningParser(is);
        Warnings warning = parser.parse();

        Assert.assertNotNull(warning.getFrom());
        Assert.assertEquals(1403679360000L, warning.getFrom().getTime());
        Assert.assertEquals(Integer.valueOf(687), warning.getNumber());
        Assert.assertEquals(expectedGaleWarnings, warning.getGale());
        Assert.assertEquals(expectedStormWarnings, warning.getStorm());
        Assert.assertEquals(expectedIceWarnings, warning.getIcing());
    }

    @Test
    public void testWithNoWarnings() throws IOException {

        Map<String, String> expectedGaleWarnings = new HashMap<>();
        Map<String, String> expectedStormWarnings = new HashMap<>();
        Map<String, String> expectedIceWarnings = new HashMap<>();
        
        InputStream is = getClass().getResourceAsStream("/dmi/gronvar-2014-07-15-warningscancelled.xml");

        DmiWarningParser parser = new DmiWarningParser(is);
        Warnings warning = parser.parse();

        Assert.assertNotNull(warning.getFrom());
        Assert.assertEquals(1405461780000L, warning.getFrom().getTime());
        Assert.assertEquals(Integer.valueOf(676), warning.getNumber());
        Assert.assertEquals(expectedGaleWarnings, warning.getGale());
        Assert.assertEquals(expectedStormWarnings, warning.getStorm());
        Assert.assertEquals(expectedIceWarnings, warning.getIcing());
    }
}
