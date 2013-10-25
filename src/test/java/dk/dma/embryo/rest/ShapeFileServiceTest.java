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

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import static org.junit.Assert.assertEquals;

public class ShapeFileServiceTest {
    private ShapeFileService service = new ShapeFileService();

    @Test
    public void test() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        service.localDmiDirectory = "/Users/chvid/sfs/dmi";
        List<ShapeFileService.Shape> file = service.getMultipleFile("dmi.201310132210_Qaanaaq_RIC", 0, "", true, 4);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gos = new GZIPOutputStream(out);
        String result = mapper.writeValueAsString(file);
        System.out.println("uncompressed size is " + result.getBytes().length);
        gos.write(result.getBytes());
        System.out.println("size is " + out.toByteArray().length);

    }

    @Test
    public void reprojectTest() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<ShapeFileService.Shape> file = service.getMultipleFile("static.world_merc", 0, "", true, 4);
        String result = mapper.writeValueAsString(file);
        System.out.println(result);
    }
}
