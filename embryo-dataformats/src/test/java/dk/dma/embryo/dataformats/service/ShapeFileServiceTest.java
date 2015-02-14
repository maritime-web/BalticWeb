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
package dk.dma.embryo.dataformats.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.GZIPOutputStream;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import dk.dma.embryo.common.configuration.PropertiesReader;
import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.dataformats.service.ShapeFileService.BaseFragment;
import dk.dma.embryo.dataformats.service.ShapeFileService.Fragment;
import dk.dma.embryo.dataformats.service.ShapeFileService.PointFragment;
import dk.dma.embryo.dataformats.service.ShapeFileService.Shape;

public class ShapeFileServiceTest {

    private PropertyFileService propertyFileService;
    private ShapeFileServiceImpl service;
    private Properties properties;

    @Before
    public void setup() throws IOException, URISyntaxException {
        properties = new PropertiesReader().read();
        properties.setProperty("embryo.iceChart.dmi.localDirectory", getClass().getResource("/ice").getPath());
        properties.setProperty("embryo.iceChart.dmi.json.SouthWest_RIC", "exponent=2;resolution=3");

        propertyFileService = new PropertyFileService(properties);

        service = new ShapeFileServiceImpl(propertyFileService);
        service.logger = LoggerFactory.getLogger(ShapeFileServiceImpl.class);
        service.init();
    }

    @Ignore
    @Test
    public void test() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ShapeFileService.Shape file = service.readSingleFile("iceChart-dmi.201311190920_CapeFarewell_RIC", 0, "", true,
                4, 0);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gos = new GZIPOutputStream(out);
        String result = mapper.writeValueAsString(file);
        System.out.println("uncompressed size is " + result.getBytes().length);
        gos.write(result.getBytes());
        gos.close();
        System.out.println("size is " + out.toByteArray().length);

    }

    @Test
    public void reprojectTest() throws IOException {
        ShapeFileService.Shape file = service.readSingleFile("static.world_merc", 0, "", true, 4, 0);
        assertEquals(-616867, ((Fragment) file.getFragments().get(0)).getPolygons().get(0).get(0).getX());
        assertEquals(170244, ((Fragment) file.getFragments().get(0)).getPolygons().get(0).get(0).getY());
    }

    @Test
    public void forecastAreas() throws IOException {
        String name = "Farvande_GRL";
        ShapeFileService.Shape shape = service.readSingleFile("static." + name, 0, "", true, 4, 0);

        assertEquals(Integer.valueOf(4), shape.getExponent());
        assertEquals(14, shape.getFragments().size());
        assertNotNull(shape.getDescription());
        assertTrue(shape.getDescription().containsKey("id"));
        assertEquals(name, shape.getDescription().get("id"));

        testFragment(shape, "Daneborg", 1L, 14);
        testFragment(shape, "Kangikajik", 2L, 14);
        testFragment(shape, "Aputiteeq", 3L, 13);
        testFragment(shape, "Kulusuk", 4L, 132);
        testFragment(shape, "Nunap Isuata Kangia", 6L, 12);
        testFragment(shape, "Nunap Isuata Kitaa", 7L, 12);
        testFragment(shape, "Nunarsuit", 8L, 12);
        testFragment(shape, "Narsalik", 9L, 12);
        testFragment(shape, "Meqquitsoq", 10L, 12);
        testFragment(shape, "Attu", 11L, 12);
        testFragment(shape, "Uiffaq", 12L, 3);
        testFragment(shape, "Qimusseriarsuaq", 13L, 23);
        testFragment(shape, "Kiatak", 14L, 1);
    }

    private void testFragment(Shape shape, String fragmentName, Long expectedId, int expectedPoints) {
        BaseFragment baseFragment = getFragment(shape, "name", fragmentName);

        assertTrue(baseFragment instanceof Fragment);
        Fragment fragment = (Fragment) baseFragment;

        assertEquals(expectedId, fragment.getDescription().get("Id"));
        assertEquals(1, fragment.getPolygons().size());
        // assertEquals(expectedPoints, fragment.getPolygons().get(0).size());
    }

    private BaseFragment getFragment(Shape shape, String key, String value) {
        for (BaseFragment fragment : shape.getFragments()) {
            if (value.equals(fragment.getDescription().get(key))) {
                return fragment;
            }
        }
        return null;
    }

    private List<BaseFragment> getFragments(Shape shape, String key, String value) {
        List<BaseFragment> fragments = new ArrayList<>();
        for (BaseFragment fragment : shape.getFragments()) {
            if (value.equals(fragment.getDescription().get(key))) {
                fragments.add(fragment);
            }
        }
        return fragments;
    }

    @Test
    public void testGreenlandInshoreIceReport() throws IOException {
        String name = "gre-inshore-icereport";
        ShapeFileService.Shape shape = service.readSingleFile("static." + name, 0, "", true, 4, 0);

        assertEquals(Integer.valueOf(4), shape.getExponent());
        assertEquals(104, shape.getFragments().size());
        assertNotNull(shape.getDescription());
        assertTrue(shape.getDescription().containsKey("id"));
        assertEquals(name, shape.getDescription().get("id"));

        testInshoreIceReportFragment(shape, "12", "Knækket", 12L, 1);
        testInshoreIceReportFragment(shape, "11", "Torssukatak", 11L, 1);
        testInshoreIceReportFragment(shape, "85", "Frederiksdal-Augpilagtoq", 85L, 3);
    }

    private void testInshoreIceReportFragment(Shape shape, String fragmentName, String expectedPlaceName,
            Long expectedNumber, int expectedCount) {

        List<BaseFragment> baseFragments = getFragments(shape, "NAME", fragmentName);
        assertEquals(expectedCount, baseFragments.size());

        for (BaseFragment baseFragment : baseFragments) {
            assertNotNull(baseFragment);
            assertTrue(baseFragment instanceof PointFragment);
            PointFragment fragment = (PointFragment) baseFragment;

            assertEquals(expectedPlaceName, fragment.getDescription().get("Placename"));
            assertEquals(expectedNumber, fragment.getDescription().get("Number"));
            assertNotNull(fragment.getPoint());

        }
    }

    @Test
    public void testResolutionDefault() throws IOException {
        ShapeFileService.Shape def = service.readSingleFile("iceChart-dmi.201304100920_CapeFarewell_RIC", null, "",
                false, null, 0);
        ShapeFileService.Shape eqDef = service.readSingleFile("iceChart-dmi.201304100920_CapeFarewell_RIC", 0, "",
                false, null, 0);
        ShapeFileService.Shape diff = service.readSingleFile("iceChart-dmi.201304100920_CapeFarewell_RIC", 4, "",
                false, null, 0);

        testResolution(def, eqDef, diff);
    }

    @Test
    public void testResolutionRegionDefault() throws IOException {
        ShapeFileService.Shape def = service.readSingleFile("iceChart-dmi.201405011000_SouthWest_RIC", null, "", false,
                null, 0);
        ShapeFileService.Shape eqDef = service.readSingleFile("iceChart-dmi.201405011000_SouthWest_RIC", 3, "", false,
                null, 0);
        ShapeFileService.Shape diff = service.readSingleFile("iceChart-dmi.201405011000_SouthWest_RIC", 4, "", false,
                null, 0);

        testResolution(def, eqDef, diff);
    }

    @Test
    public void testExponentDefault() throws IOException {
        ShapeFileService.Shape def = service.readSingleFile("iceChart-dmi.201304100920_CapeFarewell_RIC", 0, "", false,
                null, 0);
        ShapeFileService.Shape three = service.readSingleFile("iceChart-dmi.201304100920_CapeFarewell_RIC", 0, "",
                false, 3, 0);
        ShapeFileService.Shape six = service.readSingleFile("iceChart-dmi.201304100920_CapeFarewell_RIC", 0, "", false,
                6, 0);

        assertEquals(Integer.valueOf(3), def.getExponent());
        assertEquals(Integer.valueOf(3), three.getExponent());
        assertEquals(Integer.valueOf(6), six.getExponent());

        testExponents(def, three, six);
    }

    @Test
    public void testExponentRegionDefault() throws IOException {
        // Read file using default exponent;
        ShapeFileService.Shape def = service.readSingleFile("iceChart-dmi.201405011000_SouthWest_RIC", 0, "", false,
                null, 0);
        // Read file using supposed default exponent 2
        ShapeFileService.Shape eqShape = service.readSingleFile("iceChart-dmi.201405011000_SouthWest_RIC", 0, "",
                false, 2, 0);
        // Read file using exponent 6
        ShapeFileService.Shape diff = service.readSingleFile("iceChart-dmi.201405011000_SouthWest_RIC", 0, "", false,
                6, 0);

        assertEquals(Integer.valueOf(2), def.getExponent());
        assertEquals(Integer.valueOf(2), eqShape.getExponent());
        assertEquals(Integer.valueOf(6), diff.getExponent());

        testExponents(def, eqShape, diff);
    }

    private void testExponents(ShapeFileService.Shape defShape, ShapeFileService.Shape eqShape,
            ShapeFileService.Shape diffShape) {

        assertTrue(defShape.getFragments().size() == eqShape.getFragments().size());
        for (int i = 0; i < defShape.getFragments().size(); i++) {
            Fragment defFrag = (Fragment) defShape.getFragments().get(i);
            Fragment eqFrag = (Fragment) eqShape.getFragments().get(i);

            assertTrue(defFrag.getPolygons().size() == eqFrag.getPolygons().size());

            for (int j = 0; j < defFrag.getPolygons().size(); j++) {
                assertTrue(defFrag.getPolygons().get(j).size() == eqFrag.getPolygons().get(j).size());
                for (int k = 0; k < defFrag.getPolygons().get(j).size(); k++) {
                    assertTrue(defFrag.getPolygons().get(j).get(k).getX() == eqFrag.getPolygons().get(j).get(k).getX());
                    assertTrue(defFrag.getPolygons().get(j).get(k).getY() == eqFrag.getPolygons().get(j).get(k).getY());
                }
            }
        }
        assertTrue(defShape.getFragments().size() == diffShape.getFragments().size());
        for (int i = 0; i < defShape.getFragments().size(); i++) {
            Fragment defFrag = (Fragment) defShape.getFragments().get(i);
            Fragment diffFrag = (Fragment) diffShape.getFragments().get(i);

            assertTrue(defFrag.getPolygons().size() == diffFrag.getPolygons().size());

            for (int j = 0; j < defFrag.getPolygons().size(); j++) {
                // Same number of points for each polygon
                assertTrue(defFrag.getPolygons().get(j).size() == diffFrag.getPolygons().get(j).size());
                for (int k = 0; k < defFrag.getPolygons().get(j).size(); k++) {
                    // different values
                    assertFalse(defFrag.getPolygons().get(j).get(k).getX() == diffFrag.getPolygons().get(j).get(k)
                            .getX());
                    assertFalse(defFrag.getPolygons().get(j).get(k).getY() == diffFrag.getPolygons().get(j).get(k)
                            .getY());
                }
            }
        }
    }

    private void testResolution(ShapeFileService.Shape defShape, ShapeFileService.Shape eqShape,
            ShapeFileService.Shape diffShape) {

        // Assert same number of fragments
        assertTrue(defShape.getFragments().size() == eqShape.getFragments().size());
        for (int i = 0; i < defShape.getFragments().size(); i++) {
            Fragment defFrag = (Fragment) defShape.getFragments().get(i);
            Fragment eqFrag = (Fragment) eqShape.getFragments().get(i);

            // Assert same number of polygons for each fragment
            assertTrue(defFrag.getPolygons().size() == eqFrag.getPolygons().size());

            for (int j = 0; j < defFrag.getPolygons().size(); j++) {
                // Assert same number of points for each polygon
                assertTrue(defFrag.getPolygons().get(j).size() == eqFrag.getPolygons().get(j).size());
                for (int k = 0; k < defFrag.getPolygons().get(j).size(); k++) {
                    // Assert same points
                    assertTrue(defFrag.getPolygons().get(j).get(k).getX() == eqFrag.getPolygons().get(j).get(k).getX());
                    assertTrue(defFrag.getPolygons().get(j).get(k).getY() == eqFrag.getPolygons().get(j).get(k).getY());
                }
            }
        }

        // Assert same number of fragments in diff
        assertTrue(defShape.getFragments().size() == diffShape.getFragments().size());
        for (int i = 0; i < defShape.getFragments().size(); i++) {
            Fragment defFrag = (Fragment) defShape.getFragments().get(i);
            Fragment diffFrag = (Fragment) diffShape.getFragments().get(i);

            // Assert same number of polygons for each fragment
            assertTrue(defFrag.getPolygons().size() == diffFrag.getPolygons().size());

            for (int j = 0; j < defFrag.getPolygons().size(); j++) {
                // Assert different number of points for each polygon
                assertTrue(defFrag.getPolygons().get(j).size() >= diffFrag.getPolygons().get(j).size());
            }
        }
    }
}
