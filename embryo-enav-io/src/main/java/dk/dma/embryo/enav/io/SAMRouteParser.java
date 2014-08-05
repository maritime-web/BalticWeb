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
package dk.dma.embryo.enav.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import dk.dma.enav.model.geometry.Position;
import dk.dma.enav.model.voyage.Route;
import dk.dma.enav.model.voyage.RouteLeg;
import dk.dma.enav.model.voyage.RouteLeg.Heading;
import dk.dma.enav.model.voyage.Waypoint;

/**
 * Utility class for loading routes in different file formats.
 * 
 * This class is not thread safe. Nor should it be reused.
 * 
 * @author Jesper Tejlgaard
 */
public class SAMRouteParser extends RouteParser {

    // private static final Logger LOG = LoggerFactory.getLogger(RouteLoader.class);

    private Double sogDefault;
    private int wpCount = 1;

    private BufferedReader reader;

    private Route route;

    public SAMRouteParser(Reader reader) {
        if (reader instanceof BufferedReader) {
            this.reader = (BufferedReader) reader;
        } else {
            this.reader = new BufferedReader(reader);
        }
    }

    public SAMRouteParser(File file) throws FileNotFoundException {
        this(new FileReader(file));
    }

    public SAMRouteParser(InputStream io, Map<String, String> config) {
        this(new InputStreamReader(io));
    }

    public Route parse() throws IOException {
        route = new Route();

        try {
            parseHeader();
            skipSection();
            skipSection();
        } catch (FormatException e) {
            throw new IOException("Error parsing header of SAM ChartPilot route", e);
        }

        String wpDef = null;
        while ((wpDef = reader.readLine()) != null) {
            try {
                parseWp();
            } catch (FormatException e) {
                throw new IOException("Error parsing SAM ChartPilot route", e);
            }
        }
        return route;
    }

    private void parseWp() throws IOException, FormatException {
        Waypoint wp = new Waypoint();
        RouteLeg leg = new RouteLeg();
        wp.setRouteLeg(leg);

        String latitude = reader.readLine();
        String longitude = reader.readLine();
        String unknown = reader.readLine();
        String remark = reader.readLine();

        for (int i = 0; i < 5; i++) {
            reader.readLine();
        }

        unknown = reader.readLine();
        String heading = reader.readLine();
        unknown = reader.readLine();
        unknown = reader.readLine();
        String turnRad = reader.readLine();
        unknown = reader.readLine();
        unknown = reader.readLine();
        unknown = reader.readLine();
        String speed = reader.readLine();
        unknown = reader.readLine();

        reader.readLine();
        unknown = reader.readLine();
        unknown = reader.readLine();
        reader.readLine();
        unknown = reader.readLine();
        unknown = reader.readLine();
        unknown = reader.readLine();

        reader.readLine();
        unknown = reader.readLine();


        // Set defaults
        wp.setName(String.format("%04d", wpCount));
        // EPDNavSettings navSettings = (EPDNavSettings) EeINS.getSettings().getNavSettings();

        leg.setSpeed(sogDefault);
        wp.setTurnRad(getDefaults().getDefaultTurnRad());
        leg.setHeading(Heading.RL);
        leg.setXtdPort(getDefaults().getDefaultXtd());
        leg.setXtdStarboard(getDefaults().getDefaultXtd());

        if (latitude != null && longitude != null) {
            if(remark != null && remark.trim().length() > 0){
                wp.setName(remark.trim());
            }

            
            Double lat = ParseUtils.parseDouble(latitude) * 57.2957795;
            Double lon = ParseUtils.parseDouble(longitude) * 57.2957795;
            Position.verifyLatitude(lat);
            Position.verifyLongitude(lon);
            wp.setLatitude(lat);
            wp.setLongitude(lon);
            
            if("1".equals(heading)){
                leg.setHeading(Heading.RL);
            }else if ("0".equals(heading)){
                leg.setHeading(Heading.GC);
            }

            if (route.getWaypoints().size() > 0) {
                Waypoint before = route.getWaypoints().get(route.getWaypoints().size() - 1);
                double metersPerSecond = Double.parseDouble(speed);
                double knots = Calculator.metersPerSecond2Knots(metersPerSecond);
                knots = Math.round(knots * 10000) / 10000;
                before.getRouteLeg().setSpeed(knots);
            }

            if (turnRad != null && turnRad.length() > 0) {
                wp.setTurnRad(Double.parseDouble(turnRad) / 1852.0);
            }

            // if (parts[1].startsWith("1")) {
            // leg.setHeading(Heading.RL);
            // } else {
            // leg.setHeading(Heading.GC);
            // }
            // wp.setTurnRad(ParseUtils.parseDouble(parts[1]));
            // leg.setSpeed(ParseUtils.parseDouble(parts[1]));

            route.getWaypoints().add(wp);
        }

        wpCount++;
    }

    private void parseHeader() throws IOException, FormatException {
        String firstLine = reader.readLine();
        String unknown = reader.readLine();
        String name = reader.readLine();
        unknown = reader.readLine();

        // empty line
        reader.readLine();

        unknown = reader.readLine();
        unknown = reader.readLine();

        route.setName(name != null && name.trim().length() > 0 ? name : "NO NAME");

        if (sogDefault == null) {
            sogDefault = getDefaults().getDefaultSpeed();
        }
    }

    private int sectionLines(String sectionDef) {
        sectionDef = sectionDef.trim();
        String s = sectionDef.substring(1, sectionDef.length() - 1);
        String[] numbers = s.split(",");
        return Integer.parseInt(numbers[1]);
    }

    private void skipSection() throws IOException {
        int lines = sectionLines(reader.readLine());
        for (int i = 0; i < lines; i++) {
            reader.readLine();
        }
    }

}
