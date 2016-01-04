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
package dk.dma.embryo.weather.service;

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.weather.model.DistrictForecast;
import dk.dma.embryo.weather.model.RegionForecast;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Parser for reading routes in RT3 format. RT3 format is among others used by Transas ECDIS.
 * 
 * @author Jesper Tejlgaard
 */

@Named
public class DmiForecastParser_En {

    public static final Locale DEFAULT_LOCALE = new Locale("en", "UK");

    @Property("embryo.weather.dmi.parser.districts.en")
    @Inject
    public Set<String> districts;

    private boolean closeReader;

    public RegionForecast parse(InputStream is) throws IOException {
        if (is instanceof BufferedInputStream) {
            return parse((BufferedInputStream) is);
        }
        return parse(new BufferedInputStream(is));
    }

    public RegionForecast parse(File file) throws IOException {
        closeReader = true;
        return parse(new FileInputStream(file));
    }

    private RegionForecast parse(BufferedInputStream is) throws IOException {
        RegionForecast result = new RegionForecast();

        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new InputSource(is));

            // Normalize text representation
            doc.getDocumentElement().normalize();

            NodeList children = doc.getDocumentElement().getChildNodes();
            DateTime from = null;
            DateTime to = null;

            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i) instanceof Element) {
                    Element elem = (Element) children.item(i);

                    if (elem.getNodeName().equalsIgnoreCase("dato")) {
                        from = extractFrom(elem);
                        result.setFrom(from.toDate());
                    } else if (elem.getNodeName().equalsIgnoreCase("oversigttidspunkt")) {
                        String time = extractElementText(elem);
                        time = time.replace("UTC.", "UTC");
                        time = time.replace("Synopsis", "");
                        time = time.replace("synopsis", "");
                        result.setTime(time.trim());
                    } else if (elem.getNodeName().equalsIgnoreCase("gyldighed")) {
                        to = extractTo(elem, from);
                        result.setTo(to.toDate());
                    } else if (elem.getNodeName().equalsIgnoreCase("synoptic")) {
                        String text = extractElementText(elem, "oversigt");
                        result.setDesc(text);
                    } else if (districts.contains(elem.getNodeName())) {
                        result.getDistricts().add(extractDistrikt(elem));
                    } 
                }
            }
        } catch (RuntimeException | ParserConfigurationException | SAXException e) {
            throw new IOException("Error parsing weather forecast", e);
        } finally {
            if (closeReader) {
                is.close();
            }
        }

        return result;
    }

    private String prettifyDateText(String text) {
        text = text.replace(" the", "");
        text = text.replace("utc.", "");
        text = text.replace("UTC.", "");
        text = text.replace(":", "");
        text = text.replace(".", "");
        text = text.replace(",", "");
        text = text.trim();
        text = text.substring(0, 1).toLowerCase() + text.substring(1);
        return text;
    }

    public DateTime extractFrom(Element dato) throws IOException {
        String text = extractElementText(dato);
        text = prettifyDateText(text);

        DateTimeFormatter formatter = DateTimeFormat.forPattern("EEEE dd MMMM YYYY HHmm").withZone(DateTimeZone.UTC)
                .withLocale(DEFAULT_LOCALE);
        DateTime dt = formatter.parseDateTime(text);
        return dt;
    }

    public DateTime extractTo(Element gyldighed, DateTime from) throws IOException {
        String text = extractElementText(gyldighed);
        text = text.replace("Forecast, valid to ", "");
        text = text.replace("Forecast valid to ", "");
        text = text.replace(",", " " + from.getYear());
        text = prettifyDateText(text);

        DateTimeFormatter formatter = DateTimeFormat.forPattern("EEEE dd MMMM yyyy HH").withZone(DateTimeZone.UTC)
                .withLocale(DEFAULT_LOCALE);
        DateTime to = formatter.parseDateTime(text);

        if (from.getMonthOfYear() == 12 && to.getDayOfMonth() < from.getDayOfMonth()) {
            text = text.replace("" + from.getYear(), "" + (1 + from.getYear()));
            to = formatter.parseDateTime(text);
        }
        return to;
    }

    public DistrictForecast extractDistrikt(Element distrikt) throws IOException {
        DistrictForecast forecast = new DistrictForecast();

        String name = distrikt.getNodeName();

        String forecastElemName = "udsigtfor" + name;
        String wavesElemName = "waves" + name;
        String iceElemName = "ice" + name;

        if ("nunapisuateakangia".equals(name)) {
            forecastElemName = "udsigtfornunapisuatakangia";
            wavesElemName = "waveskangia";
        } else if ("nunapisuatakitaa".equals(name)) {
            forecastElemName = "udsigtfornunapisuatakitaa";
            wavesElemName = "waveskitaa";
        }

        forecast.setName(distrikt.getAttribute("name").replace(":", ""));
        forecast.setForecast(extractElementText(distrikt, forecastElemName));
        forecast.setWaves(extractElementText(distrikt, wavesElemName));
        if (isElementAvailable(distrikt, iceElemName)) {
            forecast.setIce(extractElementText(distrikt, iceElemName));
        }
        return forecast;
    }

    public boolean isElementAvailable(Element root, String elementName) throws IOException {
        NodeList uniqueList = root.getElementsByTagName(elementName);
        return uniqueList.getLength() > 0;
    }

    public String extractElementText(Element root, String elementName) throws IOException {
        NodeList uniqueList = root.getElementsByTagName(elementName);
        if (uniqueList.getLength() != 1) {
            throw new IOException("Expected exactly one <" + elementName + "> element within <" + root.getNodeName()
                    + "> element");
        }

        return extractElementText((Element) uniqueList.item(0));
    }

    public String extractElementText(Element element) throws IOException {
        List<Node> textList = new ArrayList<>();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if ("text".equals(n.getNodeName())) {
                textList.add(n);
            }
        }
        if (textList.size() != 1) {
            throw new IOException("Expected exactly one <text> element within <" + element.getNodeName() + "> element");
        }

        return trim(textList.get(0).getTextContent());
    }

    public static String trim(String input) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(input));
        StringBuffer result = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(line.trim());
        }
        return result.toString().trim();
    }
}
