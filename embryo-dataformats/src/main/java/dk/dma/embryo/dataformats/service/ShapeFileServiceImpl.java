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
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.dataformats.dbf.DbfParser;
import dk.dma.embryo.dataformats.shapefile.PolygonSplitter;
import dk.dma.embryo.dataformats.shapefile.ProjectionFileParser;
import dk.dma.embryo.dataformats.shapefile.ShapeFileParser;
import dk.dma.embryo.dataformats.shapefile.ShapeFileParser.Point;

/**
 * @author Jesper Tejlgaard
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ShapeFileServiceImpl implements ShapeFileService {

    @Inject
    @Property(value = "embryo.iceChart.providers")
    private Map<String, String> iceChartProviders;
    
    @Inject
    @Property(value = "embryo.iceberg.providers")
    private Map<String, String> icebergProviders;

    @Inject
    private PropertyFileService propertyService;

    private Map<String, String> directories = new HashMap<>();

    private Map<String, String> settings = new HashMap<>();

    @Inject
    Logger logger;

    public ShapeFileServiceImpl() {
        super();
    }

    public ShapeFileServiceImpl(PropertyFileService service) {
        super();
        this.propertyService = service;
        this.iceChartProviders = service.getMapProperty("embryo.iceChart.providers");
        this.icebergProviders = service.getMapProperty("embryo.iceberg.providers");
    }

    private void readJsonProperty(String provider, String region, String chartType) {
        String propertyKey = "embryo." + chartType + "." + provider + ".json." + (region == null ? "default" : region);
        String value = propertyService.getProperty(propertyKey);
        if (value != null) {
            for (String setting : value.split(";")) {
                String[] settingArr = setting.split("=");
                String key = chartType + "-" + provider + (region == null ? "" : "." + region) + "." + settingArr[0];
                settings.put(key, settingArr[1]);
            }
        }
    }

    @PostConstruct
    public void init() {
        populateDirectories(iceChartProviders, "iceChart");
        populateDirectories(icebergProviders, "iceberg");

        logger.info(getClass().getSimpleName() + " initialized");
    }
    
    private void populateDirectories(Map<String, String> providers, String chartType) {
        for (String providerKey : providers.keySet()) {
            String property = "embryo." + chartType + "." + providerKey + ".localDirectory";
            String value = propertyService.getProperty(property, true);
            if (value != null) {
                directories.put(chartType + "-" + providerKey, value);
            }

            readJsonProperty(providerKey, null, chartType);

            String regions = propertyService.getProperty("embryo." + chartType + "." + providerKey + ".regions");
            for (String region : regions.split(";")) {
                String[] regArr = region.split("=");
                readJsonProperty(providerKey,regArr[0], chartType);
            }
        }
    }

    private Integer getIntegerValue(Integer value, String provider, String id, String settingKey, Integer defaultValue) {
        if (value != null) {
            return value;
        }

        String key = provider + "." + id + "." + settingKey;
        if (settings.containsKey(key)) {
            return Integer.parseInt(settings.get(key));
        }

        key = provider + "." + settingKey;
        return settings.containsKey(key) ? Integer.parseInt(settings.get(key)) : defaultValue;
    }

    public Shape readSingleFile(String id, Integer resolution, String filter, boolean delta, Integer exponent,
            Integer mapParts) throws IOException {
        InputStream shpIs = null;
        InputStream dbfIs = null;
        InputStream prjIs = null;

        String projection = null;
        ShapeFileParser.File file = null;
        List<Map<String, Object>> data = null;

        int index = id.indexOf(".");
        String provider = id.substring(0, index);

        try {
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

        String region = id.substring(id.indexOf("_") + 1);
        resolution = getIntegerValue(resolution, provider, region, "resolution", 0);
        exponent = getIntegerValue(exponent, provider, region, "exponent", 2);
        mapParts = getIntegerValue(mapParts, provider, region, "parts", 0);
        
        logger.debug("resolution={}, exponent={}, parts={}", resolution, exponent, mapParts);

        List<BaseFragment> fragments = new ArrayList<>();
        boolean polylines = false;

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
            } else if(r.getShape() instanceof ShapeFileParser.Point) {
                Map<String, Object> description = data.get((int) r.getHeader().getRecordNumber() - 1);
                ShapeFileParser.Point point = (Point) r.getShape();
                Position position = reprojectAndRound(point, projection, exponent);
                fragments.add(new PointFragment(description, position));
            }
        }

        Map<String, Object> shapeDescription = new HashMap<>();

        shapeDescription.put("id", id);

        return new Shape(shapeDescription, fragments, exponent);
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
            for (double i = 0; i < input.size() - 1; i += Math.max(1, size)) {
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
