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

import dk.dma.embryo.weather.model.Warnings;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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

    @Test
    public void testWeirdFormat() throws IOException {
        InputStream is = getClass().getResourceAsStream("/dmi/gronvar-2014-09-11.xml");

        DmiWarningParser warningParser = new DmiWarningParser(is);
        Warnings warnings = new WarningTranslator().fromDanishToEnglish(warningParser.parse());
        Map<String, String> gale = warnings.getGale();
        Assert.assertEquals(6, gale.size());
        String kangikajik = gale.get("Kangikajik");
        Assert.assertNotNull(kangikajik);
        Assert.assertEquals("South 15 m/s.", kangikajik);
        String kulusuk = gale.get("Kulusuk");
        Assert.assertNotNull(kulusuk);
        Assert.assertEquals("Northernmost part  north east 13, southern part  south 18 m/s. Tonight  west south west 23 m/s.", kulusuk);
    }
}
