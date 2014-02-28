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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import dk.dma.dataformats.shapefile.ShapeFileParser.Box;
import dk.dma.dataformats.shapefile.ShapeFileParser.Point;
import dk.dma.dataformats.shapefile.ShapeFileParser.PolyLine;
import dk.dma.dataformats.shapefile.ShapeFileParser.Record;

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

    @Test
    public void readAllArcticFromAari() throws IOException {
        ShapeFileParser.File f = ShapeFileParser.parse(getClass().getResourceAsStream("/ice/aari_arc_20140211_pl_a/aari_arc_20140211_pl_a.shp"));
        assertEquals("Expected number of records", 342, f.getRecords().size());
        assertEquals("PolyLine expected", true, f.getRecords().get(0).getShape() instanceof ShapeFileParser.PolyLine);
        assertEquals("Expected number of points in first PolyLine", 6, ((ShapeFileParser.PolyLine) f.getRecords().get(0).getShape()).getNumPoints());
        assertEquals("Actual number of points in first PolyLine", ((ShapeFileParser.PolyLine) f.getRecords().get(0).getShape()).getNumPoints(),
                ((ShapeFileParser.PolyLine) f.getRecords().get(0).getShape()).getPoints().size());

        ShapeFileParser.PolyLine polyLine = (ShapeFileParser.PolyLine) f.getRecords().get(5).getShape();
        
        for(Record record : f.getRecords()){
            if(record.getShape() instanceof PolyLine){
                Box box = ((PolyLine)record.getShape()).getBox();
                System.out.println(box);
                if(box.getxMax() >= 178 && box.getxMax() <= 180 && box.getxMin() >= -180 && box.getxMin() <= -178){
                    System.out.println("juhu");
                }
                
                for(List<Point> part : ((PolyLine)record.getShape()).getPartsAsPoints()){
                    int sum = 0;
                    Point old = null;
                    for(Point point : part){
                        if(old != null){
                            if(point.getX() <= -179 && old.getX() >= 179){
                                sum -= 1;
                            }
                            if(old.getX() <= -179 && point.getX() >= 179){
                                sum += 1;
                            }
                            
                        }
                        old = point;
                    }
                    if(sum > 0){
                        System.out.println("Found it");
                    }
                }
            }
        }

        assertEquals("Sixth record is a polyline with 4 parts", 1, polyLine.getNumParts());
        assertEquals("Parts as points should split in 4 parts", 1, polyLine.getPartsAsPoints().size());

        int sum = 0;


        for (List<ShapeFileParser.Point> part : polyLine.getPartsAsPoints()) {
            sum += part.size();
        }

        assertEquals("N.o. points in all parts equals total n.o. points", polyLine.getNumPoints(), sum);
    }
}
