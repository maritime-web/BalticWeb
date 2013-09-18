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
package dk.dma.dataformats.shapefile;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ShapeFileParserTest {
    @Test
    public void readFileFromDmi() throws IOException {
        ShapeFileParser.File f = ShapeFileParser.parse(getClass().getResourceAsStream("/ice/201304100920_CapeFarewell_RIC.shp"));
        assertEquals("Expected number of records", 23, f.getRecords().size());
        assertEquals("PolyLine expected", true, f.getRecords().get(0).getShape() instanceof ShapeFileParser.PolyLine);
        assertEquals("Expected number of points in first PolyLine", 687, ((ShapeFileParser.PolyLine) f.getRecords().get(0).getShape()).getNumPoints());
        assertEquals("Actual number of points in first PolyLine", ((ShapeFileParser.PolyLine) f.getRecords().get(0).getShape()).getNumPoints(),
                ((ShapeFileParser.PolyLine) f.getRecords().get(0).getShape()).getPoints().size());

        ShapeFileParser.PolyLine polyLine = (ShapeFileParser.PolyLine) f.getRecords().get(5).getShape();

        assertEquals("Sixth record is a polyline with 4 parts", 4, polyLine.getNumParts());
        assertEquals("Parts as points should split in 4 parts", 4, polyLine.getPartsAsPoints().size());

        int sum = 0;


        for (List<ShapeFileParser.Point> part : polyLine.getPartsAsPoints()) {
            sum += part.size();
        }

        assertEquals("N.o. points in all parts equals total n.o. points", polyLine.getNumPoints(), sum);
    }
}
