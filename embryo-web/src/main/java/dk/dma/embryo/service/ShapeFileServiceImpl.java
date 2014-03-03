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

import static javax.ejb.TransactionAttributeType.SUPPORTS;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.slf4j.Logger;

import dk.dma.dataformats.dbf.DbfParser;
import dk.dma.dataformats.shapefile.PolygonSplitter;
import dk.dma.dataformats.shapefile.ProjectionFileParser;
import dk.dma.dataformats.shapefile.ShapeFileParser;
import dk.dma.embryo.configuration.Property;
import dk.dma.embryo.configuration.PropertyFileService;
import dk.dma.embryo.security.AuthorizationChecker;
import dk.dma.embryo.security.authorization.RolesAllowAll;

/**
 * @author Jesper Tejlgaard
 */
@Stateless
@TransactionAttribute(SUPPORTS)
@Interceptors(value=AuthorizationChecker.class)
public class ShapeFileServiceImpl implements ShapeFileService{
    
    @Inject
    @Property(value = "embryo.iceChart.providers")
    private Map<String, String> providers;

    @Inject
    private PropertyFileService propertyService;

    private Map<String, String> directories = new HashMap<>();

    @Inject
    Logger logger;

    public ShapeFileServiceImpl() {
        super();
    }

    public ShapeFileServiceImpl(Map<String, String> providers, Map<String, String> directories) {
        super();
        this.providers = providers;
        this.directories = directories;
    }
    
    @PostConstruct
    public void init() {
        for (String providerKey : providers.keySet()) {
            String property = "embryo.iceChart." + providerKey + ".localDirectory";
            String value = propertyService.getProperty(property, true);
            if (value != null) {
                directories.put(providerKey, value);
            }
        }
        logger.info(getClass().getSimpleName() + " initialized");
    }

    public Shape internalReadSingleFile(String id, int resolution, String filter, boolean delta, int exponent, int mapParts) throws IOException {
        InputStream shpIs = null;
        InputStream dbfIs = null;
        InputStream prjIs = null;

        String projection = null;
        ShapeFileParser.File file = null;
        List<Map<String, Object>> data = null;

        try {
            int index = id.indexOf(".");
            String provider = id.substring(0, index);

            if (directories.containsKey(provider)) {
                String localDirectory = directories.get(provider);
                id = id.substring(index + 1);
                shpIs = new FileInputStream(localDirectory + "/" + id + ".shp");
                dbfIs = new FileInputStream(localDirectory + "/" + id + ".dbf");
                prjIs = new FileInputStream(localDirectory + "/" + id + ".prj");
            } else if (id.startsWith("static.")) {
                id = id.substring(7);
                shpIs = getClass().getResourceAsStream("/shapefiles/" + id + ".shp");
                dbfIs = getClass().getResourceAsStream("/shapefiles/" + id + ".dbf");
                prjIs = getClass().getResourceAsStream("/shapefiles/" + id + ".prj");
            } else {
                throw new RuntimeException("No prefix for " + id);
            }
            projection = ProjectionFileParser.parse(prjIs);
            file = ShapeFileParser.parse(shpIs);
            data = DbfParser.parse(dbfIs);
        } finally {
            if (prjIs != null) {
                prjIs.close();
            }
            if (shpIs != null) {
                shpIs.close();
            }
            if (dbfIs != null) {
                dbfIs.close();
            }
        }

        List<Fragment> fragments = new ArrayList<>();

        for (ShapeFileParser.Record r : file.getRecords()) {
            if (r.getShape() instanceof ShapeFileParser.PolyLine) {
                Map<String, Object> description = data.get((int) r.getHeader().getRecordNumber() - 1);
                if (filter.equals("") || description.get("POLY_TYPE").equals(filter)) {

                    List<List<Position>> polygons = new ArrayList<>();

                    List<List<ShapeFileParser.Point>> parts = new PolygonSplitter(mapParts).execute(r);

                    for (List<ShapeFileParser.Point> part : parts) {
                        List<Position> polygon = new ArrayList<>();
                        for (ShapeFileParser.Point p : part) {
                            polygon.add(reprojectAndRound(p, projection, exponent));
                        }

                        if (resolution != 0) {
                            polygon = resample(polygon, resolution);
                        }

                        if (delta) {
                            polygon = convertToDelta(polygon);
                        }

                        if (polygon.size() > 1) {
                            polygons.add(polygon);
                        }
                    }

                    if (polygons.size() > 0) {
                        fragments.add(new Fragment(description, polygons));
                    }
                }
            }
        }

        Map<String, Object> shapeDescription = new HashMap<>();

        shapeDescription.put("id", id);

        return new Shape(shapeDescription, fragments);
    }

    @RolesAllowAll
    public Shape readSingleFile(String id, int resolution, String filter, boolean delta, int exponent, int mapParts) throws IOException {
        return internalReadSingleFile(id, resolution, filter, delta, exponent, mapParts);
    }

    private static Position reprojectAndRound(ShapeFileParser.Point p, String projection, int exponent) {
        if (projection.equals("GOOGLE_MERCATOR")) {
            // after http://stackoverflow.com/questions/11957538/converting-geographic-wgs-84-to-web-mercator-102100

            double x = p.getX();
            double y = p.getY();
            double num3 = x / 6378137.0;
            double num4 = num3 * 57.295779513082323;
            double num5 = Math.floor((num4 + 180.0) / 360.0);
            double num6 = num4 - (num5 * 360.0);
            double num7 = 1.5707963267948966 - (2.0 * Math.atan(Math.exp((-1.0 * y) / 6378137.0)));
            double x1 = num6;
            double y1 = num7 * 57.295779513082323;

            return new Position(round(x1, exponent), round(y1, exponent));
        } else {
            return new Position(round(p.getX(), exponent), round(p.getY(), exponent));
        }
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

    private static long round(double value, int exponent) {
        return Math.round(value * Math.pow(10, exponent));
    }

    private static List<Position> convertToDelta(List<Position> input) {
        List<Position> result = new ArrayList<>();

        Position current = new Position(0, 0);

        for (Position p : input) {
            Position delta = new Position(p.getX() - current.getX(), p.getY() - current.getY());
            if (delta.getX() != 0 || delta.getY() != 0) {
                result.add(delta);
            }
            current = p;
        }

        return result;
    }

    private static List<Position> convertFromDelta(List<Position> input) {
        List<Position> result = new ArrayList<>();

        Position current = new Position(0, 0);

        for (Position delta : input) {
            Position p = new Position(delta.getX() + current.getX(), delta.getY() + current.getY());
            result.add(p);
            current = p;
        }

        return result;
    }


}
