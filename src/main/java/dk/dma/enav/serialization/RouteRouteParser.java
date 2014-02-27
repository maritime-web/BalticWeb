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
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import dk.dma.enav.model.voyage.Route;
import dk.dma.enav.model.voyage.RouteLeg;
import dk.dma.enav.model.voyage.RouteLeg.Heading;
import dk.dma.enav.model.voyage.Waypoint;

/**
 * Parser for reading routes in ROUTE format. ROUTE format is exported using a 'VisionMaster FT' ECDIS from 'Sperry
 * Marine'. This parser was developed for a VisionMaster FT ECDIS with Software Version 4.1.
 * 
 * @author Jesper Tejlgaard
 */
/*
 * More information may be found at: https://dma-enav.atlassian.net/browse/EMBRYO-129
 */
public class RouteRouteParser extends RouteParser {

    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    // private static final Logger LOGGER = LoggerFactory.getLogger(RouteLoader.class);

    private boolean closeReader;
    private BufferedReader reader;
    private String summaryName;

    RouteDefaults defaults = new RouteDefaults();

    public RouteRouteParser(Reader reader) {
        if (reader instanceof BufferedReader) {
            this.reader = (BufferedReader) reader;
        } else {
            this.reader = new BufferedReader(reader);
        }
    }

    public RouteRouteParser(File file) throws FileNotFoundException {
        this(new FileReader(file));
        closeReader = true;
    }

    public RouteRouteParser(InputStream io, Map<String, String> config) {
        this(new InputStreamReader(io));

        summaryName = config.get("name");
    }

    private Element getSummariesElement(Document doc) throws IOException {
        NodeList summariesElements = doc.getElementsByTagName("Summaries");
        if (summariesElements == null || summariesElements.getLength() < 1) {
            throw new IOException("Failed to parse ROUTE file, no Summaries node");
        }

        for (int i = 0; i < summariesElements.getLength(); i++) {
            Element summariesElem = (Element) summariesElements.item(i);
            String name = getChildElementText(summariesElem, "Name");
            if (summaryName == null || summaryName.equalsIgnoreCase(name)) {
                return summariesElem;
            }
        }

        return null;
    }

    private String getChildElementText(Element controlPointsElement, String name) {
        NodeList routeNameElements = controlPointsElement.getElementsByTagName(name);
        if (routeNameElements.getLength() == 0) {
            return null;
        }
        Element routeNameElement = (Element) routeNameElements.item(0);
        return routeNameElement.getTextContent();
    }

    private SortedMap<Integer, Element> getControlPointsElements(Document doc, String name) throws IOException {
        SortedMap<Integer, Element> elements = new TreeMap<>();
        NodeList controlPointsElements = doc.getElementsByTagName("ControlPoints");
        if (controlPointsElements == null || controlPointsElements.getLength() < 1) {
            throw new IOException("Failed to parse ROUTE file, no ControlPoints node");
        }

        for (int i = 0; i < controlPointsElements.getLength(); i++) {
            Element controlPointsElement = (Element) controlPointsElements.item(i);
            String routeName = getChildElementText(controlPointsElement, "RouteName");
            if (name.equals(routeName)) {
                String sequenceNumber = getChildElementText(controlPointsElement, "SequenceNumber");
                elements.put(Integer.parseInt(sequenceNumber), controlPointsElement);
            }
        }

        return elements;
    }

    public Route parse() throws IOException {
        Route route = new Route();
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new InputSource(reader));
            // Normalize text representation
            doc.getDocumentElement().normalize();

            Element summariesElement = getSummariesElement(doc);
            String name = getChildElementText(summariesElement, "Name");
            String departureTime = getChildElementText(summariesElement, "DepartureTime");
            SortedMap<Integer, Element> controlPointsElements = getControlPointsElements(doc, name);

            route.setName(name);

            for (Entry<Integer, Element> entry : controlPointsElements.entrySet()) {

                Element controlPointsElement = entry.getValue();
                String controlPointsName = getChildElementText(controlPointsElement, "Name");
                String sequenceNumber = getChildElementText(controlPointsElement, "SequenceNumber");
                String latitude = getChildElementText(controlPointsElement, "Latitude");
                String longitude = getChildElementText(controlPointsElement, "Longitude");
                String turnRadius = getChildElementText(controlPointsElement, "TurnRadius");
                String departingControlLineType = getChildElementText(controlPointsElement, "DepartingControlLineType");
                String turnSpeed = getChildElementText(controlPointsElement, "DepartingTrackSpeed");

                Waypoint wp = new Waypoint();
                RouteLeg leg = new RouteLeg();
                wp.setRouteLeg(leg);
                route.getWaypoints().add(wp);
                
                Double turningRadius = Double.parseDouble(turnRadius);
                turningRadius = turningRadius/(1.852 * 1000);

                wp.setName(waypointName(controlPointsName, Integer.parseInt(sequenceNumber)));
                wp.setTurnRad(turningRadius);
                wp.setLatitude(Math.toDegrees(Double.parseDouble(latitude)));
                wp.setLongitude(Math.toDegrees(Double.parseDouble(longitude)));

                leg.setHeading("RhumbLine".equals(departingControlLineType.trim()) ? Heading.RL : Heading.GC);
                leg.setSpeed(Double.parseDouble(turnSpeed) * 1.943844492);

                // default values
                leg.setXtdPort(defaults.getDefaultXtd());
                leg.setXtdStarboard(defaults.getDefaultXtd());

                // leg.setSFWidth(sFWidth);
                // leg.setSFLen(sFLen);

            }

            if (route.getWaypoints().size() > 0) {
                route.getWaypoints().get(0).setEta(DATE_FORMAT.parse(departureTime));
            }

        } catch (IOException e) {
            throw new IOException("Error reading ROUTE file", e);
        } catch (Exception e) {
            throw new IOException("Error parsing ROUTE file", e);
        } finally {
            if (closeReader) {
                reader.close();
            }
        }

        return route;
    }
}
