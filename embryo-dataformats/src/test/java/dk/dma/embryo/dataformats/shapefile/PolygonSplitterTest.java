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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;

import dk.dma.embryo.dataformats.shapefile.PolygonSplitter.SplitPoint;
import dk.dma.embryo.dataformats.shapefile.ShapeFileParser.Point;

public class PolygonSplitterTest {

    @Test
    public void testSplitPolygon_SmallPolygonClockWise() {
        // TEST DATA
        // Rectangle polygon crossing the date line in a counter clockwise fashion (when looking at date line)
        List<Point> polygon = new ArrayList<>();
        polygon.add(new Point(175, 10));
        polygon.add(new Point(-175, 10));
        polygon.add(new Point(-175, 20));
        polygon.add(new Point(175, 20));

        // Crossings
        PolygonSplitter splitter = new PolygonSplitter(1);
        List<SplitPoint> intersections = splitter.getIntersections(polygon);

        // EXECUTE
        List<List<Point>> result = new PolygonSplitter(1).splitPolygon(polygon, intersections);

        // VERIFY RESULT
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());

        Assert.assertNotNull(result.get(0));
        ReflectionAssert.assertReflectionEquals(
                Arrays.asList(new Point(-180, 10), polygon.get(1), polygon.get(2), new Point(-180, 20)), result.get(0));
        ReflectionAssert.assertReflectionEquals(
                Arrays.asList(polygon.get(0), new Point(180, 10), new Point(180, 20), polygon.get(3)), result.get(1));
    }

    @Test
    public void testSplitPolygon_SmallPolygonCounterClockWise() {
        // TEST DATA
        // Rectangle polygon crossing the date line in a counter clockwise fashion (when looking at date line)
        List<Point> polygon = new ArrayList<>();
        polygon.add(new Point(-175, 10));
        polygon.add(new Point(175, 10));
        polygon.add(new Point(175, 20));
        polygon.add(new Point(-175, 20));

        // Crossings
        PolygonSplitter splitter = new PolygonSplitter(1);
        List<SplitPoint> intersections = splitter.getIntersections(polygon);

        // EXECUTE
        List<List<Point>> result = new PolygonSplitter(1).splitPolygon(polygon, intersections);

        // VERIFY RESULT
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());

        Assert.assertNotNull(result.get(0));
        ReflectionAssert.assertReflectionEquals(
                Arrays.asList(new Point(180, 10), polygon.get(1), polygon.get(2), new Point(180, 20)), result.get(0));
        ReflectionAssert.assertReflectionEquals(
                Arrays.asList(polygon.get(0), new Point(-180, 10), new Point(-180, 20), polygon.get(3)), result.get(1));

    }

    @Test
    public void testSplitPolygonInto4Parts() {
        // TEST DATA
        // Rectangle polygon crossing the date line in a counter clockwise fashion (when looking at date line)
        List<Point> polygon = new ArrayList<>();
        polygon.add(new Point(175, 10));
        polygon.add(new Point(-175, 10));
        polygon.add(new Point(-120, 10));
        polygon.add(new Point(-85, 10));
        polygon.add(new Point(-85, 20));
        polygon.add(new Point(-120, 20));
        polygon.add(new Point(-175, 20));
        polygon.add(new Point(175, 20));

        // Crossings
        PolygonSplitter splitter = new PolygonSplitter(8);
        List<SplitPoint> intersections = splitter.getIntersections(polygon);

        // EXECUTE
        List<List<Point>> result = splitter.splitPolygon(polygon, intersections);

        // VERIFY RESULT
        Assert.assertNotNull(result);
        Assert.assertEquals(4, result.size());

        ReflectionAssert.assertReflectionEquals(
                Arrays.asList(new Point(-90, 10), new Point(-85, 10), new Point(-85, 20), new Point(-90, 20)),
                result.get(0));
        ReflectionAssert.assertReflectionEquals(Arrays.asList(new Point(-135, 10), new Point(-120, 10), new Point(-90,
                10), new Point(-90, 20), new Point(-120, 20), new Point(-135, 20)), result.get(1));
        ReflectionAssert.assertReflectionEquals(Arrays.asList(new Point(-180, 10), new Point(-175, 10), new Point(-135,
                10), new Point(-135, 20), new Point(-175, 20), new Point(-180, 20)), result.get(2));
        ReflectionAssert.assertReflectionEquals(
                Arrays.asList(new Point(175, 10), new Point(180, 10), new Point(180, 20), new Point(175, 20)),
                result.get(3));

    }

    @Test
    public void testSplitPolygon_SimpleRoundTheWorld() {
        // TEST DATA
        // Polygon crossing the date line in a counter clockwise fashion (when looking at date line)
        List<Point> polygon = new ArrayList<>();
        polygon.add(new Point(175, 60));
        polygon.add(new Point(-175, 60));
        polygon.add(new Point(-120, 60));
        polygon.add(new Point(-80, 60));
        polygon.add(new Point(-30, 60));
        polygon.add(new Point(10, 60));
        polygon.add(new Point(70, 60));
        polygon.add(new Point(110, 60));

        PolygonSplitter splitter = new PolygonSplitter(4);

        // EXECUTE
        List<SplitPoint> intersections = splitter.getIntersections(polygon);
        List<List<Point>> result = splitter.splitPolygon(polygon, intersections);

        // VERIFY RESULT
        Assert.assertNotNull(result);
        Assert.assertEquals(5, result.size());
        
        System.out.println(result.get(0));

        ReflectionAssert.assertReflectionEquals(
                Arrays.asList(new Point(90, 60), new Point(110, 60), new Point(110, 89), new Point(90, 89)),
                result.get(0));
        ReflectionAssert.assertReflectionEquals(Arrays.asList(new Point(0, 60), new Point(10, 60), new Point(70, 60),
                new Point(90, 60), new Point(90, 89), new Point(70, 89), new Point(10, 89), new Point(0, 89)), result
                .get(1));
        ReflectionAssert.assertReflectionEquals(Arrays.asList(new Point(-90, 60), new Point(-80, 60),
                new Point(-30, 60), new Point(0, 60), new Point(0, 89), new Point(-30, 89), new Point(-80, 89),
                new Point(-90, 89)), result.get(2));
        ReflectionAssert.assertReflectionEquals(Arrays.asList(new Point(-180, 60), new Point(-175, 60), new Point(-120,
                60), new Point(-90, 60), new Point(-90, 89), new Point(-120, 89), new Point(-175, 89), new Point(-180,
                89)), result.get(3));
        ReflectionAssert.assertReflectionEquals(
                Arrays.asList(new Point(175, 60), new Point(180, 60), new Point(180, 89), new Point(175, 89)),
                result.get(4));
    }

    @Test
    public void testSplitPolygon_ComplexRoundTheWorld() {
        // TEST DATA
        // Polygon crossing the date line in a counter clockwise fashion (when looking at date line)
        List<Point> polygon = new ArrayList<>();
        polygon.add(new Point(175, 60));
        polygon.add(new Point(-175, 60));
        polygon.add(new Point(175, 40));
        polygon.add(new Point(175, 30));
        polygon.add(new Point(-175, 20));
        polygon.add(new Point(-20, 20));
        polygon.add(new Point(40, -40));
        polygon.add(new Point(170, 60));

        PolygonSplitter splitter = new PolygonSplitter(2);

        // EXECUTE
        List<SplitPoint> intersections = splitter.getIntersections(polygon);
        List<List<Point>> result = splitter.splitPolygon(polygon, intersections);

        // VERIFY RESULT
        Assert.assertNotNull(result);
        // Assert.assertEquals(4, result.size());

        ReflectionAssert.assertReflectionEquals(
                Arrays.asList(new Point(180, 50), new Point(175, 40), new Point(175, 30), new Point(180, 25)),
                result.get(0));
        ReflectionAssert.assertReflectionEquals(Arrays.asList(new Point(0, 0), new Point(40, -40), new Point(170, 60),
                new Point(170, 89), new Point(40, 89), new Point(0, 89)), result.get(1));
        ReflectionAssert.assertReflectionEquals(Arrays.asList(new Point(-180, 60), new Point(-175, 60), new Point(-180,
                50), new Point(-180, 25), new Point(-175, 20), new Point(-20, 20), new Point(0, 0), new Point(0, 89),
                new Point(-20, 89), new Point(-175, 89), new Point(-180, 89)), result.get(2));
        ReflectionAssert.assertReflectionEquals(Arrays.asList(new Point(175, 60), new Point(180, 60), new Point(180, 89),
                new Point(175, 89)), result.get(3));
    }

    //@Test
    public void testSplitPolygon_ComplexRoundTheWorld2() {
        // TEST DATA
        // Polygon crossing the date line in a counter clockwise fashion (when looking at date line)
        List<Point> polygon = new ArrayList<>();
        polygon.add(new Point(-14, 78));
        polygon.add(new Point(-10, 79));
        polygon.add(new Point(-12, 80));
        polygon.add(new Point(-16, 82));
        polygon.add(new Point(-16, 82));
        polygon.add(new Point(-175, 82));
        polygon.add(new Point(175, 78));
        polygon.add(new Point(170, 90));
        polygon.add(new Point(175, 30));
        polygon.add(new Point(-175, 20));
        polygon.add(new Point(-20, 20));
        polygon.add(new Point(40, -40));
        polygon.add(new Point(170, 60));

        PolygonSplitter splitter = new PolygonSplitter(2);

        // EXECUTE
        List<SplitPoint> intersections = splitter.getIntersections(polygon);
        List<List<Point>> result = splitter.splitPolygon(polygon, intersections);

        // VERIFY RESULT
        Assert.assertNotNull(result);
        // Assert.assertEquals(4, result.size());

        ReflectionAssert.assertReflectionEquals(
                Arrays.asList(new Point(180, 50), new Point(175, 40), new Point(175, 30), new Point(180, 25)),
                result.get(0));
        ReflectionAssert.assertReflectionEquals(Arrays.asList(new Point(0, 0), new Point(40, -40), new Point(170, 60),
                new Point(170, 89), new Point(40, 89), new Point(0, 89)), result.get(1));
        ReflectionAssert.assertReflectionEquals(Arrays.asList(new Point(-180, 60), new Point(-175, 60), new Point(-180,
                50), new Point(-180, 25), new Point(-175, 20), new Point(-20, 20), new Point(0, 0), new Point(0, 89),
                new Point(-20, 89), new Point(-175, 89), new Point(-180, 89)), result.get(2));
        ReflectionAssert.assertReflectionEquals(Arrays.asList(new Point(175, 60), new Point(180, 60), new Point(180, 89),
                new Point(175, 89)), result.get(3));
    }
}
