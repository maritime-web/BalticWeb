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
package dk.dma.embryo.rest;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class ShapeFileServiceTest {

    private ShapeFileService service;

    @Before
    public void setup() {
        Map<String, String> providers = new HashMap<String, String>();
        providers.put("dmi", "DMI");
        Map<String, String> directories = new HashMap<String, String>();
        directories.put("dmi", "/Users/chvid/sfs/dmi");
        service = new ShapeFileService(providers, directories);
    }

    @Ignore
    @Test
    public void test() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        service.logger = LoggerFactory.getLogger(ShapeFileService.class);
        List<ShapeFileService.Shape> file = service
                .getMultipleFile("dmi.201311190920_CapeFarewell_RIC", 0, "", true, 4);

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
        service.logger = LoggerFactory.getLogger(ShapeFileService.class);
        List<ShapeFileService.Shape> file = service.getMultipleFile("static.world_merc", 0, "", true, 4);
        assertEquals(-616867, file.get(0).getFragments().get(0).getPolygons().get(0).get(0).getX());
        assertEquals(170244, file.get(0).getFragments().get(0).getPolygons().get(0).get(0).getY());
    }
}
