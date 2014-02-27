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
package dk.dma.embryo.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Jesper Tejlgaard
 */
public interface ShapeFileService {

    Shape internalReadSingleFile(String id, int resolution, String filter, boolean delta, int exponent, int mapParts)
            throws IOException;

    Shape readSingleFile(String id, int resolution, String filter, boolean delta, int exponent, int mapParts)
            throws IOException;

    public static class Position {
        private long x;
        private long y;

        public Position(long x, long y) {
            this.x = x;
            this.y = y;
        }

        public long getX() {
            return x;
        }

        public long getY() {
            return y;
        }

        public String toString() {
            return "Position(" + x + ", " + y + ")";
        }
    }

    public static class Fragment {
        private List<List<Position>> polygons;
        private Map<String, Object> description;

        public Fragment(Map<String, Object> description, List<List<Position>> polygons) {
            this.polygons = polygons;
            this.description = description;
        }

        public List<List<Position>> getPolygons() {
            return polygons;
        }

        public Map<String, Object> getDescription() {
            return description;
        }
    }

    public static class Shape {
        private List<Fragment> fragments;
        private Map<String, Object> description;

        public Shape(Map<String, Object> description, List<Fragment> fragments) {
            this.fragments = fragments;
            this.description = description;
        }

        public List<Fragment> getFragments() {
            return fragments;
        }

        public Map<String, Object> getDescription() {
            return description;
        }
    }

}
