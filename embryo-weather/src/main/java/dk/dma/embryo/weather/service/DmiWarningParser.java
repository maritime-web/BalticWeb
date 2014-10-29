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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import dk.dma.embryo.weather.model.Warnings;

/**
 * Parser for reading routes in RT3 format. RT3 format is among others used by
 * Transas ECDIS.
 * 
 * @author Jesper Tejlgaard
 */
public class DmiWarningParser {

    public static final Locale DEFAULT_LOCALE = new Locale("da", "DK");

    private boolean closeReader;
    private BufferedInputStream is;

    public DmiWarningParser(InputStream is) {
        if (is instanceof BufferedInputStream) {
            this.is = (BufferedInputStream) is;
        } else {
            this.is = new BufferedInputStream(is);
        }
    }

    public DmiWarningParser(File file) throws FileNotFoundException {
        this(new FileInputStream(file));
    }

    public Warnings parse() throws IOException {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new InputSource(is));

            // Normalize text representation
            doc.getDocumentElement().normalize();
            String text = extractElementText(doc.getDocumentElement(), "danskvarsel");
            return parseGaleWarnings(text);
        } catch (RuntimeException | ParserConfigurationException | SAXException e) {
            throw new IOException("Error parsing gale warning", e);
        } finally {
            if (closeReader) {
                is.close();
            }
        }
    }

    private Warnings parseGaleWarnings(String input) throws IOException {
        Warnings result = new Warnings();

        BufferedReader reader = new BufferedReader(new StringReader(input));

        String date = reader.readLine();
        date = prettifyDateText(date);
        DateTimeFormatter formatter = DateTimeFormat.forPattern("EEEE dd MMMM yyyy HH:mm").withZone(DateTimeZone.UTC).withLocale(DEFAULT_LOCALE);
        DateTime ts = formatter.parseDateTime(date);
        result.setFrom(ts.toDate());

        Map<String, String> warnings = null;
        String line;
        boolean useMetersPerSecond = false;
        while ((line = reader.readLine()) != null) {
            if (line.trim().length() != 0) {
                if (line.indexOf("Varsel nummer") == 0 && line.indexOf("ophører") < 0) {
                    result.setNumber(Integer.valueOf(line.substring(14)));
                } else if (line.indexOf("Kulingvarsel") >= 0 || line.indexOf("kulingvarsel") >= 0) {
                    warnings = result.getGale();
                    useMetersPerSecond = true;
                } else if (line.indexOf("Stormvarsel") >= 0 || line.indexOf("stormvarsel") >= 0) {
                    warnings = result.getStorm();
                    useMetersPerSecond = false;
                } else if (line.indexOf("Overisningsvarsel") >= 0 || line.indexOf("overisningsvarsel") >= 0) {
                    warnings = result.getIcing();
                    useMetersPerSecond = false;
                } else if (warnings != null) {
                    String value = null;
                    StringBuilder sb = new StringBuilder();
                    while (((value = reader.readLine()) != null) && !value.trim().equals("")) {
                        // String value = reader.readLine();
                        if (useMetersPerSecond) {
                            value = value.replace(" m/s.", ".");
                            value = value.replace("m/s.", ".");
                            value = value.replace(".", " m/s.");
                        }
                        sb.append(value + " ");
                    }
                    String name = line.replace(":", "");
                    warnings.put(name, sb.toString().trim());
                }
            }
        }
        return result;
    }

    private String prettifyDateText(String text) {
        text = text.replace(" den", "");
        text = text.replace("utc.", "");
        text = text.replace(".", "");
        text = text.replace(",", "");
        text = text.trim();
        text = text.substring(0, 1).toLowerCase() + text.substring(1);
        return text;
    }

    public String extractElementText(Element root, String elementName) throws IOException {
        NodeList uniqueList = root.getElementsByTagName(elementName);
        if (uniqueList.getLength() != 1) {
            throw new IOException("Expected exactly one <" + elementName + "> element within <" + root.getNodeName() + "> element");
        }

        return extractElementText((Element) uniqueList.item(0));
    }

    public String extractElementText(Element element) throws IOException {
        NodeList uniquetextList = element.getElementsByTagName("text");
        if (uniquetextList.getLength() != 1) {
            throw new IOException("Expected exactly one <text> element within <" + element.getNodeName() + "> element");
        }

        return uniquetextList.item(0).getTextContent();
    }
}
