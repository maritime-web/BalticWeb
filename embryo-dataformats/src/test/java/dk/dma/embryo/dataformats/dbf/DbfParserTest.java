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
package dk.dma.embryo.dataformats.dbf;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class DbfParserTest {
    @Test
    public void readFileFromDmi() throws IOException {
        List<Map<String,Object>> result = DbfParser.parse(getClass().getResourceAsStream("/ice/201307222045_CapeFarewell_RIC.dbf"));

        assertEquals(10, result.size());
        assertEquals("3", result.get(0).get("FA"));
        assertEquals("W", result.get(9).get("POLY_TYPE"));
    }
}
