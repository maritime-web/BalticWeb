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
package dk.dma.embryo.dataformats.service;

import java.io.IOException;
import java.util.HashMap;
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
            this.description = new HashMap<>();
            this.description.putAll(description);
            this.description.remove("Size_m");
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
