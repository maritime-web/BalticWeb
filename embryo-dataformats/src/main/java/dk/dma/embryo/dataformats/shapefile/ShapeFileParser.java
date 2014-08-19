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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class ShapeFileParser {
    private static int readInt(InputStream is) throws IOException {
        byte[] bytes = new byte[4];
        int read = is.read(bytes);
        if (read != 4) {
            throw new RuntimeException("Expected to read 4 bytes, read " + read);
        }
        return ByteBuffer.wrap(bytes).getInt();
    }

    private static int readIntLittle(InputStream is) throws IOException {
        byte[] bytes = new byte[4];
        int read = is.read(bytes);
        if (read != 4) {
            throw new RuntimeException("Expected to read 4 bytes, read " + read);
        }
        ByteBuffer wrapper = ByteBuffer.wrap(bytes);
        wrapper.order(ByteOrder.LITTLE_ENDIAN);
        return wrapper.getInt();
    }

    private static double readDoubleLittle(InputStream is) throws IOException {
        byte[] bytes = new byte[8];
        int read = is.read(bytes);
        if (read != 8) {
            throw new RuntimeException("Expected to read 8 bytes, read " + read);
        }
        ByteBuffer wrapper = ByteBuffer.wrap(bytes);
        wrapper.order(ByteOrder.LITTLE_ENDIAN);
        return wrapper.getDouble();
    }

    public static File parse(InputStream is) throws IOException {
        try {
            File f = new File();

            f.header = FileHeader.read(is);
            f.records = new ArrayList<>();
            
            long sum = 50;

            do {
                Record r = new Record();

                r.header = RecordHeader.read(is); // 8

                int shapeId = readIntLittle(is); // 4

                switch (shapeId) {
                    // Point
                    case 1:
                        r.shape = Point.read(is);
                        break;
                    // Polyline
                    case 3:
                        r.shape = PolyLine.read(is);
                        break;
                    // Polygon
                    case 5:
                        r.shape = PolyLine.read(is);
                        break;
                    // Null shape
                    case 0:
                        is.read(new byte[Math.max(0, (int) r.header.contentLength * 2 - 4)]);
                        r.shape = new Unknown();
                        break;
                    default:
                        throw new RuntimeException("Unknown shape id: " + shapeId + " in stream " + is + " position: " + sum);

                }

                if (shapeId == 0) {
                    sum += 6;
                } else {
                    sum += r.header.contentLength + 4;
                }
                f.records.add(r);
            } while (sum < f.header.fileLength);

            return f;
        } finally {
            is.close();
        }
    }

    public static class Point extends Shape {
        private double x;
        private double y;
        
        public Point(double x, double y) {
            super();
            this.x = x;
            this.y = y;
        }

        public static Point read(InputStream is) throws IOException {
            double x = readDoubleLittle(is);
            double y = readDoubleLittle(is);
            return new Point(x, y);
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public String toString() {
            return "{ x: " + x + ", y: " + y + "}";
        }
    }

    public static class Box {
        private double xMin;
        private double yMin;
        private double xMax;
        private double yMax;

        public static Box read(InputStream is) throws IOException {
            Box b = new Box();
            b.xMin = readDoubleLittle(is);
            b.yMin = readDoubleLittle(is);
            b.xMax = readDoubleLittle(is);
            b.yMax = readDoubleLittle(is);
            return b;
        }

        public double getxMin() {
            return xMin;
        }

        public double getyMin() {
            return yMin;
        }

        public double getxMax() {
            return xMax;
        }

        public double getyMax() {
            return yMax;
        }

        @Override
        public String toString() {
            return "Box [xMin=" + xMin + ", yMin=" + yMin + ", xMax=" + xMax + ", yMax=" + yMax + "]";
        }
        
    }

    public static class RecordHeader {
        private long recordNumber;
        private long contentLength;

        public static RecordHeader read(InputStream is) throws IOException {
            RecordHeader h = new RecordHeader();
            h.recordNumber = readInt(is);
            h.contentLength = readInt(is);
            return h;
        }

        public long getRecordNumber() {
            return recordNumber;
        }

        public long getContentLength() {
            return contentLength;
        }
    }

    public static class FileHeader {
        private long fileCode;
        private long fileLength;
        private long version;
        private long shapeType;
        private double xMin;
        private double yMin;
        private double xMax;
        private double yMax;
        private double zMin;
        private double zMax;
        private double mMin;
        private double mMax;

        public static FileHeader read(InputStream is) throws IOException {
            FileHeader h = new FileHeader();
            h.fileCode = readInt(is);
            readInt(is); // unused
            readInt(is); // unused
            readInt(is); // unused
            readInt(is); // unused
            readInt(is); // unused
            h.fileLength = readInt(is);
            h.version = readIntLittle(is);
            h.shapeType = readIntLittle(is);
            h.xMin = readDoubleLittle(is);
            h.xMax = readDoubleLittle(is);
            h.yMin = readDoubleLittle(is);
            h.yMax = readDoubleLittle(is);
            h.zMin = readDoubleLittle(is);
            h.zMax = readDoubleLittle(is);
            h.mMin = readDoubleLittle(is);
            h.mMax = readDoubleLittle(is);
            return h;
        }

        public long getFileCode() {
            return fileCode;
        }

        public long getFileLength() {
            return fileLength;
        }

        public long getVersion() {
            return version;
        }

        public long getShapeType() {
            return shapeType;
        }

        public double getxMin() {
            return xMin;
        }

        public double getyMin() {
            return yMin;
        }

        public double getxMax() {
            return xMax;
        }

        public double getyMax() {
            return yMax;
        }

        public double getzMin() {
            return zMin;
        }

        public double getzMax() {
            return zMax;
        }

        public double getmMin() {
            return mMin;
        }

        public double getmMax() {
            return mMax;
        }
    }

    public abstract static class Shape {
    }

    public static class PolyLine extends Shape {
        private Box box;
        private int numParts;
        private int numPoints;
        private List<Integer> parts;
        private List<Point> points;

        public static PolyLine read(InputStream is) throws IOException {
            PolyLine pl = new PolyLine();
            pl.box = Box.read(is);
            pl.numParts = readIntLittle(is);
            pl.numPoints = readIntLittle(is);
            pl.parts = new ArrayList<>();
            for (int i = 0; i < pl.numParts; i++) {
                pl.parts.add(readIntLittle(is));
            }
            pl.points = new ArrayList<>();
            for (int i = 0; i < pl.numPoints; i++) {
                pl.points.add(Point.read(is));
            }
            return pl;
        }

        public List<List<Point>> getPartsAsPoints() {
            List<List<Point>> result = new ArrayList<>();

            for (int i = 0; i < parts.size(); i++) {
                int start = parts.get(i);
                int end = (i == parts.size() - 1) ? points.size() : parts.get(i + 1);
                result.add(points.subList(start, end));
            }

            return result;

        }

        public Box getBox() {
            return box;
        }

        public int getNumParts() {
            return numParts;
        }

        public int getNumPoints() {
            return numPoints;
        }

        public List<Integer> getParts() {
            return parts;
        }

        public List<Point> getPoints() {
            return points;
        }
    }

    public static class Unknown extends Shape {
    }

    public static class Record {
        private RecordHeader header;
        private Shape shape;

        public RecordHeader getHeader() {
            return header;
        }

        public Shape getShape() {
            return shape;
        }
    }

    public static class File {
        private FileHeader header;
        private List<Record> records;

        public FileHeader getHeader() {
            return header;
        }

        public List<Record> getRecords() {
            return records;
        }
    }
}
