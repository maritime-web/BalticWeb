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
package dk.dma.embryo.rest;

import dk.dma.configuration.Property;
import dk.dma.dataformats.dbf.DbfParser;
import dk.dma.dataformats.shapefile.ShapeFileParser;
import org.jboss.resteasy.annotations.GZIP;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/shapefile")
public class ShapeFileService {
    @Inject
    @Property(value = "embryo.iceMaps.localDmiDirectory", substituteSystemProperties = true)
    private String localDmiDirectory;

    @GET
    @Path("/single/{id}")
    @Produces("application/json")
    @GZIP
    public Shape getSingleFile(
            @PathParam("id") String id,
            @DefaultValue("0") @QueryParam("resolution") int resolution,
            @DefaultValue("") @QueryParam("filter") String filter
    ) throws IOException {
        return readSingleFile(id, resolution, filter);
    }

    @GET
    @Path("/multiple/{ids}")
    @Produces("application/json")
    @GZIP
    public List<Shape> getMultipleFile(
            @PathParam("ids") String ids,
            @DefaultValue("0") @QueryParam("resolution") int resolution,
            @DefaultValue("") @QueryParam("filter") String filter
    ) throws IOException {
        List<Shape> result = new ArrayList<>();

        for (String id : ids.split(",")) {
            result.add(readSingleFile(id, resolution, filter));
        }

        return result;
    }

    public Shape readSingleFile(String id, int resolution, String filter) throws IOException {
        Map<String, Object> shapeDescription = new HashMap<>();
        shapeDescription.put("id", id);
        List<Fragment> fragments = new ArrayList<>();

        ShapeFileParser.File file = ShapeFileParser.parse(new FileInputStream(localDmiDirectory + "/" + id + ".shp"));
        List<Map<String, Object>> data = DbfParser.parse(new FileInputStream(localDmiDirectory + "/" + id + ".dbf"));

        for (ShapeFileParser.Record r : file.getRecords()) {
            if (r.getShape() instanceof ShapeFileParser.PolyLine) {
                Map<String, Object> description = data.get((int) r.getHeader().getRecordNumber() - 1);
                if (filter.equals("") || description.get("POLY_TYPE").equals(filter)) {
                    List<List<Position>> polygons = new ArrayList<>();

                    for (List<ShapeFileParser.Point> part : ((ShapeFileParser.PolyLine) r.getShape()).getPartsAsPoints()) {
                        List<Position> polygon = new ArrayList<>();
                        for (ShapeFileParser.Point p : part) {
                            polygon.add(new Position(p.getX(), p.getY()));
                        }

                        if (resolution == 0) {
                            polygons.add(polygon);
                        } else {
                            polygons.add(resample(polygon, resolution));
                        }
                    }
                    fragments.add(new Fragment(description, polygons));
                }
            }
        }

        return new Shape(shapeDescription, fragments);
    }

    private static <T> List<T> resample(List<T> input, int size) {
        size--;

        List<T> result = new ArrayList<>();

        if (input.size() > size) {
            for (double i = 0; i < input.size() - 1; i += Math.max(1, (double) input.size() / size)) {
                result.add(input.get((int) Math.floor(i)));
            }

            result.add(input.get(input.size() - 1));
        } else {
            result.addAll(input);
        }

        return result;
    }

    public static void main(String[] args) {
        System.out.println("result is " + resample(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20), 4));
    }

    public static class Position {
        private double x;
        private double y;

        public Position(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
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
