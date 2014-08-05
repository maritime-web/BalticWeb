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
package dk.dma.embryo.dataformats.shapefile;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import dk.dma.embryo.dataformats.shapefile.ShapeFileParser.Box;
import dk.dma.embryo.dataformats.shapefile.ShapeFileParser.Point;
import dk.dma.embryo.dataformats.shapefile.ShapeFileParser.PolyLine;
import dk.dma.embryo.dataformats.shapefile.ShapeFileParser.Record;

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
