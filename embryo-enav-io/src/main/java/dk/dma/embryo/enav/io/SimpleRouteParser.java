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
import java.util.Date;
import java.util.Map;

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
public class SimpleRouteParser extends RouteParser {

    // private static final Logger LOG = LoggerFactory.getLogger(RouteLoader.class);

    private BufferedReader reader;
    
    private Route route;
    private Waypoint waypoint;

    public SimpleRouteParser(Reader reader) {
        if (reader instanceof BufferedReader) {
            this.reader = (BufferedReader) reader;
        } else {
            this.reader = new BufferedReader(reader);
        }
    }

    public SimpleRouteParser(File file) throws FileNotFoundException {
        this(new FileReader(file));
    }

    public SimpleRouteParser(InputStream io, Map<String, String> config)  {
        this(new InputStreamReader(io));
    }

    private static final String FORMAT_ERR_MSG = "Error in route format.";

    public Route parse() throws IOException {
        route = null;
        try {
            boolean firstLine = true;
            String line = null;
            while ((line = reader.readLine()) != null) {
                // Ignore empty lines and comments
                if (line.length() == 0 || line.startsWith("//") || line.startsWith("#")) {
                    continue;
                }
                // Split line by tab
                String[] fields = line.split("\t");
                // Handle first line name\tdeparture\tdestination
                if (firstLine) {
                    if (fields.length == 0) {
                        // LOG.error("First line has no fields: " + line);
                        throw newException("First line must contain at least route name.", line);
                    }
                    String destination = null;
                    String departure = null;
                    if (fields.length >= 3) {
                        destination = fields[2];
                    }
                    if (fields.length >= 2) {
                        departure = fields[1];
                    }
                    firstLine = false;

                    route = createRoute(fields[0], departure, destination);
                } else {
                    // Handle waypoint lines
                    if (fields.length < 7) {
                        // LOG.error("Waypoint line has less than seven fields: " + line);
                        throw newException("Waypoint line has less than seven fields", line);
                    }

                    double latitude = 0;
                    double longitude = 0;
                    Double turnRadius = null;
                    Double speed = null;
                    Heading heading = null;
                    // Get position
                    try {
                        latitude = ParseUtils.parseLatitude(fields[1]);
                        longitude = ParseUtils.parseLongitude(fields[2]);
                    } catch (FormatException e) {
                        throw newException("Error in position", line);
                    }
                    // Get turn radius
                    try {
                        turnRadius = ParseUtils.parseDouble(fields[6].trim());
                    } catch (FormatException e) {
                        throw newException("Error in turn radius", line);
                    }

                    // Get speed
                    try {
                        speed = ParseUtils.parseDouble(fields[3].trim());
                    } catch (FormatException e) {
                        throw newException("Error in speed", line);
                    }

                    waypoint = createWaypoint(fields[0], null, latitude, longitude, null, turnRadius);

                    // Get heading
                    try {
                        heading = Heading.valueOf(ParseUtils.parseInt(fields[4].trim()));
                    } catch (FormatException e) {
                        throw newException("Error in heading", line);
                    }

                    // Get XTD
                    String xtd = fields[5];
                    String xtdStarboard = xtd;
                    String xtdPort = xtd;
                    if (xtd.contains(",")) {
                        String[] xtdParts = xtd.split(",");
                        if (xtdParts.length != 2) {
                            throw newException("Error in XTD", line);
                        }
                        xtdStarboard = xtdParts[0];
                        xtdPort = xtdParts[1];
                    }

                    Double xtdDouble = null;
                    Double xtdStarboardDouble = null;
                    try {
                        xtdStarboardDouble = ParseUtils.parseDouble(xtdStarboard.trim());
                        xtdDouble = ParseUtils.parseDouble(xtdPort.trim());
                    } catch (FormatException e) {
                        throw newException("Error in XTD", line);
                    }

                    createWaypointLeg(speed, heading, xtdDouble, xtdStarboardDouble);
                }
            }
            reader.close();
        } catch (IOException e) {
            // LOG.error("Failed to load route file: " + e.getMessage());
            throw e;
        }

        return route;
    }

    private Route createRoute(String name, String destination, String departure) {
        return new Route(name, destination, departure);
    }

    private Waypoint createWaypoint(String name, Date eta, double latitude, double longitude, Double rot, Double turnRad) {
        Waypoint w = new Waypoint(name, latitude, longitude, rot, turnRad);
        route.getWaypoints().add(w);
        return w;
    }

    private RouteLeg createWaypointLeg(Double speed, Heading heading, Double xtdPort, Double xtdStarboard) {
        RouteLeg leg = new RouteLeg(speed, heading, xtdPort, xtdStarboard);
        waypoint.setRouteLeg(leg);
        return leg;
    }
    
    private IOException newException(String msg, String line){
        return new IOException(FORMAT_ERR_MSG + " " + msg + ". " + line);
    }
}
