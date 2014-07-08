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
}
