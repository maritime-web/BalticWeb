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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import dk.dma.enav.model.geometry.Position;
import dk.dma.enav.model.voyage.Route;
import dk.dma.enav.model.voyage.RouteLeg;
import dk.dma.enav.model.voyage.RouteLeg.Heading;
import dk.dma.enav.model.voyage.Waypoint;

/**
 * Parser for reading routes in RT3 format. RT3 format is among others used by Transas ECDIS.
 * 
 * @author Jesper Tejlgaard
 */
public class Rt3RouteParser extends RouteParser {

    // private static final Logger LOGGER = LoggerFactory.getLogger(RouteLoader.class);

    private boolean closeReader;
    private BufferedReader reader;
    private String scheduleName;

    RouteDefaults defaults = new RouteDefaults();

    public Rt3RouteParser(Reader reader) {
        if (reader instanceof BufferedReader) {
            this.reader = (BufferedReader) reader;
        } else {
            this.reader = new BufferedReader(reader);
        }
    }

    public Rt3RouteParser(File file) throws FileNotFoundException {
        this(new FileReader(file));
        closeReader = true;
    }

    public Rt3RouteParser(InputStream io, Map<String, String> config) {
        this(new InputStreamReader(io));
        
        scheduleName = config.get("name");
    }

    public Route parse() throws IOException {

        Route route = new Route();

        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new InputSource(reader));
            // Normalize text representation
            doc.getDocumentElement().normalize();

            // Get name
            route.setName(doc.getDocumentElement().getAttribute("RtName"));
            if (route.getName() == null) {
                route.setName("NO NAME");
            }

            // Get waypoints
            NodeList waypointsList = doc.getElementsByTagName("WayPoints");
            if (waypointsList == null || waypointsList.getLength() < 1) {
                throw new IOException("Failed to parse RT3, no WayPoints node");
            }
            Element waypointsNode = (Element) waypointsList.item(0);
            NodeList waypoints = waypointsNode.getElementsByTagName("WayPoint");
            if (waypoints == null || waypoints.getLength() == 0) {
                throw new IOException("Failed to parse RT3, no WayPoint nodes");
            }

            NodeList wpsExList = null;
            NodeList calculationsList = doc.getElementsByTagName("Calculations");
            if (calculationsList != null && calculationsList.getLength() >= 1) {
                Element calculationsNode = (Element) calculationsList.item(0);
                NodeList calculationList = calculationsNode.getElementsByTagName("Calculation");
                if (calculationList != null) {
                    for (int i = 0; i < calculationList.getLength(); i++) {
                        Element calculationElem = (Element) calculationList.item(i);
                        String name = calculationElem.getAttribute("CalcName");
                        if (scheduleName != null && scheduleName.equals(name)) {
                            wpsExList = calculationElem.getElementsByTagName("WayPointEx");
                            break;
                        }
                    }
                }
            }
            // Log "No <Calculations> element. Assuming default values for Speed, Heading"

            // Iterate thorugh Waypoints/Waypoint && Calculations/Calculation[name="x"]/WaypointExs/waypointEx
            for (int i = 0; i < waypoints.getLength(); i++) {
                // Get waypoint element
                Element wpElem = (Element) waypoints.item(i);
                // Create route objects
                Waypoint wp = new Waypoint();

                // Set defaults
                wp.setTurnRad(getDefaults().getDefaultTurnRad());

                // Wp name
                String name = wpElem.getAttribute("WPName");
                wp.setName(waypointName(name, i+1));

                // Lat and lon
                Double lat = ParseUtils.parseDouble(wpElem.getAttribute("Lat"));
                Double lon = ParseUtils.parseDouble(wpElem.getAttribute("Lon"));
                if (lat == null || lon == null) {
                    throw new IOException("Missing latitude/longitude for WP " + wp.getName());
                }

                // RT3 position are given in minutes. Transform to degrees
                lat = lat / 60;
                lon = lon / 60;

                Position pos = Position.create(lat, lon);

                wp.setLatitude(pos.getLatitude());
                wp.setLongitude(pos.getLongitude());

                // Turn rad
                String turnRad = wpElem.getAttribute("TurnRadius");
                if (turnRad != null && turnRad.length() > 0) {
                    wp.setTurnRad(ParseUtils.parseDouble(turnRad));
                }

                if (i < waypoints.getLength()) {
                    RouteLeg leg = new RouteLeg();
                    wp.setRouteLeg(leg);
                    leg.setSpeed(getDefaults().getDefaultSpeed());
                    leg.setXtdPort(getDefaults().getDefaultXtd());
                    leg.setXtdStarboard(getDefaults().getDefaultXtd());

                    if (wpsExList != null && i < wpsExList.getLength()) {
                        Element wpsEx = (Element) wpsExList.item(i);
                        String speedStr = wpsEx.getAttribute("Speed");
                        leg.setSpeed(Double.valueOf(speedStr));
                    }

                    // XTE
                    String xte = wpElem.getAttribute("PortXTE");
                    if (xte != null && xte.length() > 0) {
                        leg.setXtdPort(ParseUtils.parseDouble(xte));
                    }
                    xte = wpElem.getAttribute("StbXTE");
                    if (xte != null && xte.length() > 0) {
                        leg.setXtdStarboard(ParseUtils.parseDouble(xte));
                    }
                    // Leg type
                    String legType = wpElem.getAttribute("LegType");
                    if (legType != null && !legType.equals("0")) {
                        leg.setHeading(Heading.GC);
                    } else {
                        leg.setHeading(Heading.RL);
                    }
                }
                route.getWaypoints().add(wp);
            }

        } catch (IOException e) {
            // LOG.error("Failed to load RT3 route file: " + e.getMessage());
            throw new IOException("Error reading route file", e);
        } catch (Exception e) {
            // LOG.error("Failed to parse RT3 route file: " + e.getMessage());
            throw new IOException("Error parsing RT3 route file", e);
        } finally{
            if(closeReader){
                reader.close();
            }
        }

        return route;
    }
}
