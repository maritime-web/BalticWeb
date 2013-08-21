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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class ShapeFileParser {
    private static int readInt(InputStream is) throws IOException {
        byte[] bytes = new byte[4];
        is.read(bytes);
        return ByteBuffer.wrap(bytes).getInt();
    }

    private static int readIntLittle(InputStream is) throws IOException {
        byte[] bytes = new byte[4];
        is.read(bytes);
        ByteBuffer wrapper = ByteBuffer.wrap(bytes);
        wrapper.order(ByteOrder.LITTLE_ENDIAN);
        return wrapper.getInt();
    }

    private static double readDoubleLittle(InputStream is) throws IOException {
        byte[] bytes = new byte[8];
        is.read(bytes);
        ByteBuffer wrapper = ByteBuffer.wrap(bytes);
        wrapper.order(ByteOrder.LITTLE_ENDIAN);
        return wrapper.getDouble();
    }

    public static class Point {
        private double x;
        private double y;

        public static Point read(InputStream is) throws IOException {
            Point p = new Point();
            p.x = readDoubleLittle(is);
            p.y = readDoubleLittle(is);
            return p;
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

    public static abstract class Shape {
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
            for (int i = 0; i < pl.numParts; i++) pl.parts.add(readIntLittle(is));
            pl.points = new ArrayList<>();
            for (int i = 0; i < pl.numPoints; i++) pl.points.add(Point.read(is));
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

    public static File parse(InputStream is) throws IOException {
        try {
            File f = new File();

            f.header = FileHeader.read(is);
            f.records = new ArrayList<>();

            long sum = 0;

            do {
                Record r = new Record();

                r.header = RecordHeader.read(is);

                int shapeId = readIntLittle(is);

                switch (shapeId) {
                    case 5:
                        r.shape = PolyLine.read(is);
                        break;
                    default:
                        is.read(new byte[Math.max(0, (int) r.header.contentLength * 2 - 8)]);
                        r.shape = new Unknown();
                        break;
                }

                sum += r.header.contentLength + 4;

                f.records.add(r);
            } while (sum < f.header.fileLength);

            return f;
        } finally {
            is.close();
        }
    }
}
