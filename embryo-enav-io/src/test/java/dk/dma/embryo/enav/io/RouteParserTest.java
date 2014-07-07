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
package dk.dma.embryo.enav.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author Jesper Tejlgaard
 */
public class RouteParserTest {

    @Test
    public void testGetRouteParser() throws IOException {
        // Data

        InputStream is = new ByteArrayInputStream(new byte[] {});
        Map<String, String> config = new HashMap<>();

        RouteParser parser = RouteParser.getRouteParser("map1234t", is, config);
        Assert.assertEquals(SAMRouteParser.class, parser.getClass());
    }

}
