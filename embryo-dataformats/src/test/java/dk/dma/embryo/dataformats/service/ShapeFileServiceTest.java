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
package dk.dma.embryo.dataformats.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.zip.GZIPOutputStream;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import dk.dma.embryo.common.configuration.PropertiesReader;
import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.dataformats.service.ShapeFileService.Fragment;

public class ShapeFileServiceTest {

    private PropertyFileService propertyFileService;
    private ShapeFileServiceImpl service;
    private Properties properties;

    @Before
    public void setup() throws IOException, URISyntaxException {
        properties = new PropertiesReader().read();
        properties.setProperty("embryo.iceChart.dmi.localDirectory", getClass().getResource("/ice").getPath());
        properties.setProperty("embryo.iceChart.dmi.json.SouthWest_RIC","exponent=2;resolution=3");

        propertyFileService = new PropertyFileService(properties);

        service = new ShapeFileServiceImpl(propertyFileService);
        service.logger = LoggerFactory.getLogger(ShapeFileServiceImpl.class);
        service.init();
    }

    @Ignore
    @Test
    public void test() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ShapeFileService.Shape file = service.readSingleFile("iceChart-dmi.201311190920_CapeFarewell_RIC", 0, "", true, 4, 0);

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
        assertEquals(-616867, ((Fragment)file.getFragments().get(0)).getPolygons().get(0).get(0).getX());
        assertEquals(170244, ((Fragment)file.getFragments().get(0)).getPolygons().get(0).get(0).getY());
    }

    @Test
    public void forecastAreas() throws IOException {
//        System.out.println("forecastAreas");
//        
//        String name = "Farvande_GRL_modified";
//        ShapeFileService.Shape shape = service.readSingleFile("static." + name, 0, "", true, 2, 0);
//        
//        assertEquals(Integer.valueOf(2), shape.getExponent());
//        assertTrue(shape.getFragments().size() > 0);
//        assertNotNull(shape.getDescription());
//        assertTrue(shape.getDescription().containsKey("id"));
//        assertEquals(name, shape.getDescription().get("id"));
    }

    
    @Test
    public void testResolutionDefault() throws IOException {
        ShapeFileService.Shape def = service.readSingleFile("iceChart-dmi.201304100920_CapeFarewell_RIC", null, "", false, null, 0);
        ShapeFileService.Shape eqDef = service.readSingleFile("iceChart-dmi.201304100920_CapeFarewell_RIC", 0, "", false, null, 0);
        ShapeFileService.Shape diff = service.readSingleFile("iceChart-dmi.201304100920_CapeFarewell_RIC", 4, "", false, null, 0);

        testResolution(def, eqDef, diff);
    }

    @Test
    public void testResolutionRegionDefault() throws IOException {
        ShapeFileService.Shape def = service.readSingleFile("iceChart-dmi.201405011000_SouthWest_RIC", null, "", false, null, 0);
        ShapeFileService.Shape eqDef = service.readSingleFile("iceChart-dmi.201405011000_SouthWest_RIC", 3, "", false, null, 0);
        ShapeFileService.Shape diff = service.readSingleFile("iceChart-dmi.201405011000_SouthWest_RIC", 4, "", false, null, 0);

        testResolution(def, eqDef, diff);
    }

    
    @Test
    public void testExponentDefault() throws IOException {
        ShapeFileService.Shape def = service.readSingleFile("iceChart-dmi.201304100920_CapeFarewell_RIC", 0, "", false, null, 0);
        ShapeFileService.Shape three = service.readSingleFile("iceChart-dmi.201304100920_CapeFarewell_RIC", 0, "", false, 3, 0);
        ShapeFileService.Shape six = service.readSingleFile("iceChart-dmi.201304100920_CapeFarewell_RIC", 0, "", false, 6, 0);

        assertEquals(Integer.valueOf(3), def.getExponent());
        assertEquals(Integer.valueOf(3), three.getExponent());
        assertEquals(Integer.valueOf(6), six.getExponent());
        
        testExponents(def, three, six);
    }

    @Test
    public void testExponentRegionDefault() throws IOException {
        // Read file using default exponent;
        ShapeFileService.Shape def = service.readSingleFile("iceChart-dmi.201405011000_SouthWest_RIC", 0, "", false, null, 0);
        // Read file using supposed default exponent 2
        ShapeFileService.Shape eqShape = service.readSingleFile("iceChart-dmi.201405011000_SouthWest_RIC", 0, "", false, 2, 0);
        // Read file using exponent 6
        ShapeFileService.Shape diff = service.readSingleFile("iceChart-dmi.201405011000_SouthWest_RIC", 0, "", false, 6, 0);

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
