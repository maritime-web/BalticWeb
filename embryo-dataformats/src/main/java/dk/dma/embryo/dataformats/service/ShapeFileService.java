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
package dk.dma.embryo.dataformats.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Jesper Tejlgaard
 */
public interface ShapeFileService {

    Shape readSingleFile(String id, Integer resolution, String filter, boolean delta, Integer exponent, Integer mapParts)
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
    
    public static class BaseFragment {
        protected Map<String, Object> description;
        
        public Map<String, Object> getDescription() {
            return description;
        }
    }

    public static class Fragment extends BaseFragment {
        private List<List<Position>> polygons;

        public Fragment(Map<String, Object> description, List<List<Position>> polygons) {
            this.polygons = polygons;
            this.description = description;
        }

        public List<List<Position>> getPolygons() {
            return polygons;
        }
    }
    
    public static class PointFragment extends BaseFragment {
        private Position point;
        
        public PointFragment(Map<String, Object> description, Position point) {
            this.point = point;
            this.description = description;
        }
        
        public Position getPoint() {
            return point;
        }
    }

    public static class Shape {
        private List<BaseFragment> fragments;
        private Map<String, Object> description;
        private Integer exponent;

        public Shape(Map<String, Object> description, List<BaseFragment> fragments, Integer exponent) {
            this.fragments = fragments;
            this.description = description;
            this.exponent = exponent;
        }

        public List<BaseFragment> getFragments() {
            return fragments;
        }

        public Map<String, Object> getDescription() {
            return description;
        }

        public Integer getExponent() {
            return exponent;
        }
    }

}
