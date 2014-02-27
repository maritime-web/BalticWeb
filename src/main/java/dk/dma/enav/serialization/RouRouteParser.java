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
package dk.dma.enav.serialization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

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
public class RouRouteParser extends RouteParser {

    // private static final Logger LOG = LoggerFactory.getLogger(RouteLoader.class);

    private Double sogDefault;
    private int wpCount = 1;

    private BufferedReader reader;

    private Route route;

    public RouRouteParser(Reader reader) {
        if (reader instanceof BufferedReader) {
            this.reader = (BufferedReader) reader;
        } else {
            this.reader = new BufferedReader(reader);
        }
    }

    public RouRouteParser(File file) throws FileNotFoundException {
        this(new FileReader(file));
    }

    public RouRouteParser(InputStream io, Map<String, String> config) {
        this(new InputStreamReader(io));
    }

    public Route parse() throws IOException {
        route = new Route();
        String line = null;
        
        while ((line = reader.readLine()) != null) {
            try {
                // Check for header
                if (line.startsWith("ROUTE HEADER INFORMATION")) {
                    parseHeader();
                }
                else if (line.startsWith("WAYPOINT")) {
                    parseWp();
                }
            } catch (FormatException e) {
                throw new IOException("ROU Parse error: " + e.getMessage());
            }
        }
        return route;
    }

    private void parseWp() throws IOException, FormatException {
        String line = null;
        Waypoint wp = new Waypoint();
        RouteLeg leg = new RouteLeg();
        wp.setRouteLeg(leg);

        Double lat = null;
        Double lon = null;
    
        // Set defaults
        wp.setName(String.format("%03d", wpCount));
//        EPDNavSettings navSettings = (EPDNavSettings) EeINS.getSettings().getNavSettings();
        leg.setSpeed(sogDefault);  
        
        wp.setTurnRad(getDefaults().getDefaultTurnRad());
        leg.setXtdPort(getDefaults().getDefaultXtd());
        leg.setXtdStarboard(getDefaults().getDefaultXtd());
        
        while ((line = reader.readLine()) != null) {
            String str = line.trim();
            if (str.length() == 0) {
                break;
            }
            String[] parts = parsePair(str);
            if (parts[0].equals("Name")) {
                wp.setName(parts[1]);
            }
            else if (parts[0].startsWith("Latitude")) {
                lat = ParseUtils.parseDouble(parts[1]);
            }
            else if (parts[0].startsWith("Longitude")) {
                lon = ParseUtils.parseDouble(parts[1]);
            }
            else if (parts[0].startsWith("Turn radius")) {
                wp.setTurnRad(ParseUtils.parseDouble(parts[1]));
            }
            else if (parts[0].startsWith("SOG")) {
                leg.setSpeed(ParseUtils.parseDouble(parts[1]));
            }
            else if (parts[0].startsWith("Leg type")) {
                if (parts[1].startsWith("1")) {
                    leg.setHeading(Heading.RL);
                } else {
                    leg.setHeading(Heading.GC);
                }                
            }
            else if (parts[0].startsWith("Circles")) {
                String[] circleItems = StringUtils.split(parts[1]);
                if (circleItems.length != 5) {
                    throw new IOException("Error parsing ROU circles: " + parts[1]);
                }
                leg.setXtdPort(ParseUtils.parseDouble(circleItems[2]));
                leg.setXtdStarboard(ParseUtils.parseDouble(circleItems[4]));
            }
        }        
        
        // Set position
        if (lat == null || lon == null) {
            throw new IOException("Missing latitude/longitude for WP " + wp.getName());
        }
        Position.verifyLatitude(lat);
        Position.verifyLongitude(lon);
        wp.setLatitude(lat);
        wp.setLongitude(lon);
        
        route.getWaypoints().add(wp);
        
        wpCount++;
    }

    private void parseHeader() throws IOException, FormatException {
        String line = null;
        while ((line = reader.readLine()) != null) {
            String str = line.trim();
            if (str.length() == 0) {
                break;
            }
            String[] parts = parsePair(str);
            
            if (parts[0].startsWith("Route name")) {
                route.setName(parts[1]);
            } else if (parts[0].startsWith("SOG default")) {
                sogDefault = ParseUtils.parseDouble(parts[1]);
            }

        }
        // Set default name if none given
        if (route.getName() == null) {
            route.setName("NO NAME");
        }

        if (sogDefault == null) {
            sogDefault = getDefaults().getDefaultSpeed();
        }
    }

    
    private static String[] parsePair(String str) throws IOException {
        String[] parts = StringUtils.splitByWholeSeparator(str, ": ");
        if (parts.length != 2) {
            throw new IOException("Error in ROU key value pair: " + str);
        }
        return parts;
    }
}
