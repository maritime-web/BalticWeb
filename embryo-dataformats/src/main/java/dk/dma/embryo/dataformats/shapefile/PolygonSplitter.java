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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.embryo.dataformats.shapefile.ShapeFileParser.Box;
import dk.dma.embryo.dataformats.shapefile.ShapeFileParser.Point;
import dk.dma.embryo.dataformats.shapefile.ShapeFileParser.PolyLine;
import dk.dma.embryo.dataformats.shapefile.ShapeFileParser.Record;

/**
 * @author Jesper Tejlgaard
 */
public class PolygonSplitter {

    private final Logger logger = LoggerFactory.getLogger(PolygonSplitter.class);

    private double[] longitudinalSplits;
    int size = 360;

    public PolygonSplitter(int parts) {
        if (parts < 0) {
            throw new IllegalArgumentException("Parts must be zero or a positive number");
        }

        longitudinalSplits = new double[parts];

        if (parts != 0) {
            if (360 % parts != 0) {
                throw new IllegalArgumentException("360/" + longitudinalSplits + " must be whole number");
            }
            size = 360 / parts;
            int index = 1;

            for (int longitude = -180 + size; longitude < 180; longitude += size) {
                this.longitudinalSplits[index++] = longitude;
            }
            this.longitudinalSplits[0] = 180;
        }
    }

    public List<List<Point>> execute(Record record) {
        PolyLine shape = (PolyLine) record.getShape();
        if (this.longitudinalSplits.length == 0) {
            return shape.getPartsAsPoints();
        }

        Box box = shape.getBox();
        if (box.getxMax() - box.getxMin() < size) {
            // polygon is smaller than longitudinal size of map parts. Just return it.
            return shape.getPartsAsPoints();
        }
        logger.debug("Shape Box Xmax={}, Xmin={} and Xmax-Xmin={}", box.getxMax(), box.getxMin(),
                box.getxMax() - box.getxMin());

        List<List<Point>> newParts = new ArrayList<>(shape.getPartsAsPoints().size() + 10);

        // LongitudinalSplitter splitter = new LongitudinalSplitter();

        for (List<Point> part : shape.getPartsAsPoints()) {
            List<SplitPoint> intersections = getIntersections(part);

            logger.debug("Polygon split points {}", intersections.size());

            if (intersections.size() > 0) {
                newParts.addAll(splitPolygon(part, intersections));
            } else {
                newParts.add(part);
            }
        }

        return newParts;
    }

    private boolean intersects(Point p1, Point p2, double lon) {
        if (lon == 180 && p1.getX() >= 175 && p2.getX() <= -175) {
            return true;
        } else if (lon == 180 && p1.getX() <= -175 && p2.getX() >= 175) {
            return true;
        } else if (lon != 180 && (p1.getX() < lon && p2.getX() > lon) || (p2.getX() < lon && p1.getX() > lon)) {
            return true;
        }
        return false;
    }

    private SplitPoint calculateIntersectionPoint(Point point1, Point point2, double splitLon) {
        // approximate date line equation
        Point p1 = point1.getX() < point2.getX() ? point1 : point2;
        Point p2 = point1.getX() < point2.getX() ? point2 : point1;
        // point to point equation (Rhumb line)
        // P2y = m(P2x-P1x)+P1y
        // P2y-P1y = m(P2x-P1x)
        // m = (P2y-P1y)/(P2x-P1x)

        double p1x = p2.getX() - p1.getX() >= 340 ? 360 + p1.getX() : p1.getX();

        double xdiff = p2.getX() - p1x;
        double ydiff = p2.getY() - p1.getY();
        double m = ydiff / xdiff;

        // line equation: y = m(x-P1x) + P1y
        // intersection: (180, y) where y = m(180-P1x) + P1y
        double y = m * (splitLon - p1x) + p1.getY();

        return new SplitPoint(point1, point2, new Point(splitLon, y));
    }

    List<SplitPoint> getIntersections(List<Point> part) {
        Point old = null;
        List<SplitPoint> splitPoints = new LinkedList<>();
        for (Point point : part) {
            if (old != null) {
                for (double lon : longitudinalSplits) {
                    if (intersects(old, point, lon)) {
                        splitPoints.add(calculateIntersectionPoint(old, point, lon));
                        break;
                    }
                }
            }
            old = point;
        }
        return splitPoints;
    }

    // public static class LongitudinalSplitter {
    // // private double[] longitudinalSplitLines = { 180, 90, 0, -90 };
    // private double[] longitudinalSplitLines = { 180 };
    //
    // private double splitLon = -360;
    // private Point point1;
    // private Point point2;
    //
    // public boolean intersects(Point p1, Point p2) {
    // for (double lon : longitudinalSplitLines) {
    // if (lon == 180 && p1.getX() >= 175 && p2.getX() <= -175) {
    // setValues(p1, p2, lon);
    // return true;
    // } else if (lon == 180 && p1.getX() <= -175 && p2.getX() >= 175) {
    // setValues(p1, p2, lon);
    // return true;
    // } else if (lon != 180 && (p1.getX() < lon && p2.getX() > lon) || (p2.getX() < lon && p1.getX() > lon)) {
    // setValues(p1, p2, lon);
    // return true;
    // }
    // }
    // this.splitLon = -360;
    // this.point1 = null;
    // this.point2 = null;
    // return false;
    // }
    //
    // public SplitPoint getSplitPoint() {
    // // approximate date line equation
    // Point p1 = point1.getX() < point2.getX() ? point1 : point2;
    // Point p2 = point1.getX() < point2.getX() ? point2 : point1;
    // // point to point equation (Rhumb line)
    // // P2y = m(P2x-P1x)+P1y
    // // P2y-P1y = m(P2x-P1x)
    // // m = (P2y-P1y)/(P2x-P1x)
    //
    // double p2x = p1.getX() - p2.getX() > 350 ? 360 + p2.getX() : p2.getX();
    //
    // double xdiff = p2x - p1.getX();
    // double ydiff = p2.getY() - p1.getY();
    // double m = ydiff / xdiff;
    //
    // // line equation: y = m(x-P1x) + P1y
    // // intersection: (180, y) where y = m(180-P1x) + P1y
    // double y = m * (this.splitLon - p1.getX()) + p1.getY();
    //
    // return new SplitPoint(point1, point2, new Point(this.splitLon, y));
    // }
    //
    // private void setValues(Point p1, Point p2, double lon) {
    // this.splitLon = lon;
    // this.point1 = p1;
    // this.point2 = p2;
    // }
    // }


    private TreeMap<Double, List<SplitPoint>> buildMap(List<SplitPoint> intersections) {
        TreeMap<Double, List<SplitPoint>> map = new TreeMap<>();
        for (SplitPoint sp : intersections) {
            List<SplitPoint> list = map.get(sp.getSplitPoint().getX());
            if (list == null) {
                list = new LinkedList<>();
                map.put(sp.getSplitPoint().getX(), list);
            }
            list.add(sp);
        }

        for (List<SplitPoint> splitPoints : map.values()) {
            Collections.sort(splitPoints, new Comparator<SplitPoint>() {
                @Override
                public int compare(SplitPoint o1, SplitPoint o2) {
                    return Double.compare(o1.getSplitPoint().getY(), o2.getSplitPoint().getY());
                }
            });
        }

        return map;
    }

    List<List<Point>> splitPolygon(List<Point> part, List<SplitPoint> intersections) {
        TreeMap<Double, List<SplitPoint>> mapIntersections = buildMap(intersections);
        // Point[] mostNorhternLine = mostNorthernDateLineCrossing(crosses);

        List<List<Point>> resultingPolygons = new ArrayList<>(intersections.size() / 2 + 1);
        LinkedList<List<Point>> polygonStack = new LinkedList<>();
        LinkedList<SplitPoint> matchingSplits = new LinkedList<>();
        polygonStack.add(new ArrayList<Point>());

        for (Point p : part) {
            polygonStack.getLast().add(p);

            if (matchingSplits.size() > 0 && matchingSplits.getLast().getPoint1() == p) {
                Point sp = matchingSplits.getLast().getSplitPoint();
                Direction dir = matchingSplits.getLast().getDirection();
                double x = sp.getX() == 180 && dir == Direction.WEST_2_EAST ? -sp.getX() : sp.getX();
                polygonStack.getLast().add(new Point(x, sp.getY()));

                List<Point> lastPolygon = polygonStack.removeLast();
                resultingPolygons.add(lastPolygon);

                x = sp.getX() == 180 && dir == Direction.EAST_2_WEST ? -sp.getX() : sp.getX();
                polygonStack.getLast().add(new Point(x, sp.getY()));
                matchingSplits.removeLast();
            } else {
                int crossingPointIndex = -1;
                for (Entry<Double, List<SplitPoint>> entry : mapIntersections.entrySet()) {
                    List<SplitPoint> crosses = entry.getValue();

                    crossingPointIndex = getCrossingPointIndex(p, crosses);
                    if (crossingPointIndex > -1) {
                        // Add intersection point
                        Point sp = crosses.get(crossingPointIndex).getSplitPoint();
                        Direction dir = crosses.get(crossingPointIndex).getDirection();
                        double x = sp.getX() == 180 && dir == Direction.WEST_2_EAST ? -sp.getX() : sp.getX();
                        polygonStack.getLast().add(new Point(x, sp.getY()));

                        polygonStack.add(new ArrayList<Point>());

                        // add crossing on both sides
                        x = sp.getX() == 180 && dir == Direction.EAST_2_WEST ? -sp.getX() : sp.getX();
                        polygonStack.getLast().add(new Point(x, sp.getY()));

                        int matchingIndex = crossingPointIndex % 2 > 0 ? crossingPointIndex - 1
                                : crossingPointIndex + 1;
                        if (matchingIndex < crosses.size()) {
                            matchingSplits.add(crosses.get(matchingIndex));
                        } else {
                            Point p1 = crosses.get(crossingPointIndex).getPoint1();
                            Point p2 = crosses.get(crossingPointIndex).getPoint2();
                            matchingSplits.add(new SplitPoint(new Point(p2.getX(), 89), new Point(p1.getX(), 89),
                                    new Point(sp.getX(), 89)));
                        }
                        break;
                    }
                }

                
                if (part.get(part.size() - 1) == p && crossingPointIndex == -1 && matchingSplits.size() > 0){
                    SplitPoint sp = matchingSplits.getLast();
                    if(sp.getPoint1().getX() != p.getX() || sp.getPoint1().getY() != 89){
                        polygonStack.getLast().add(new Point(p.getX(), 89));
                    }
                }
            }

        }

        if (matchingSplits.size() > 0) {
            while (!matchingSplits.isEmpty()) {
                SplitPoint spoint = matchingSplits.removeLast();
                Point sp = spoint.getSplitPoint();
                Direction dir = spoint.getDirection();

                polygonStack.getLast().add(spoint.getPoint1());
                double x = sp.getX() == 180 && dir == Direction.WEST_2_EAST ? -sp.getX() : sp.getX();
                polygonStack.getLast().add(new Point(x, sp.getY()));

                List<Point> lastPolygon = polygonStack.removeLast();
                resultingPolygons.add(lastPolygon);
                // TODO add crossing twice

                x = sp.getX() == 180 && dir == Direction.EAST_2_WEST ? -sp.getX() : sp.getX();
                polygonStack.getLast().add(new Point(x, sp.getY()));
                polygonStack.getLast().add(spoint.getPoint2());
            }
        }

        List<Point> lastPolygon = polygonStack.removeLast();
        resultingPolygons.add(lastPolygon);

        return resultingPolygons;
    }

    int getCrossingPointIndex(Point p, List<SplitPoint> splitPoints) {
        for (int i = 0; i < splitPoints.size(); i++) {
            SplitPoint splitPoint = splitPoints.get(i);
            if (p == splitPoint.getPoint1()) {
                return i;
            }
        }
        return -1;
    }

    // private Point[] mostNorthernDateLineCrossing(List<Point[]> crosses){
    // Point mostNorhternIntersectionPoint = null;
    // Point[] mostNorhternLine = null;
    // for(Point[] line : crosses){
    // if(mostNorhternIntersectionPoint == null){
    // mostNorhternIntersectionPoint = dateLineIntersection(line);
    // mostNorhternLine = line;
    // }else{
    // Point intersectionPoint = dateLineIntersection(line);
    // if(mostNorhternIntersectionPoint.getY() < intersectionPoint.getY()){
    // mostNorhternIntersectionPoint = intersectionPoint;
    // mostNorhternLine = line;
    // }
    // }
    // }
    // return mostNorhternLine;
    // }

    Point dateLineIntersection(Point... points) {
        // approximate date line equation
        Point p1 = points[0].getX() >= 0 ? points[0] : points[1];
        Point p2 = points[0].getX() >= 0 ? points[1] : points[0];
        // point to point equation (Rhumb line)
        // P2y = m(P2x-P1x)+P1y
        // P2y-P1y = m(P2x-P1x)
        // m = (P2y-P1y)/(P2x-P1x)
        double p2x = 360 + p2.getX();
        double xdiff = p2x - p1.getX();
        double ydiff = p2.getY() - p1.getY();
        double m = ydiff / xdiff;

        // line equation: y = m(x-P1x) + P1y
        // intersection: (180, y) where y = m(180-P1x) + P1y
        double y = m * (180 - p1.getX()) + p1.getY();
        return new Point(points[0].getX() >= 0 ? 180 : -180, y);
    }

    public static enum Direction {
        EAST_2_WEST, WEST_2_EAST;
    }

    public static class SplitPoint {
        private Point point1;
        private Point point2;
        private Point splitPoint;
        private Direction direction;

        public SplitPoint(Point point1, Point point2, Point splitPoint) {
            super();
            this.point1 = point1;
            this.point2 = point2;
            this.splitPoint = splitPoint;
            this.direction = Direction.WEST_2_EAST;

            if (point2.getX() <= -175 && point1.getX() >= 175) {
                this.direction = Direction.EAST_2_WEST;
            } else if (point1.getX() <= -175 && point2.getX() >= 175) {
                this.direction = Direction.WEST_2_EAST;
            } else if (point2.getX() < point1.getX()) {
                this.direction = Direction.EAST_2_WEST;
            }
        }

        public Point getPoint1() {
            return point1;
        }

        public Point getPoint2() {
            return point2;
        }

        public Point getSplitPoint() {
            return splitPoint;
        }

        public Direction getDirection() {
            return direction;
        }

        @Override
        public String toString() {
            return "SplitPoint [point1=" + point1 + ", point2=" + point2 + ", splitPoint=" + splitPoint
                    + ", direction=" + direction + "]";
        }
    }
}
