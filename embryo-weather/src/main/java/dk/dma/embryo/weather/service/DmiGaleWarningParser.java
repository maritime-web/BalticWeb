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

import dk.dma.embryo.weather.model.GaleWarning;

/**
 * Parser for reading routes in RT3 format. RT3 format is among others used by Transas ECDIS.
 * 
 * @author Jesper Tejlgaard
 */
public class DmiGaleWarningParser {

    public static final Locale DEFAULT_LOCALE = new Locale("da", "DK");

    private boolean closeReader;
    private BufferedInputStream is;

    public DmiGaleWarningParser(InputStream is) {
        if (is instanceof BufferedInputStream) {
            this.is = (BufferedInputStream) is;
        } else {
            this.is = new BufferedInputStream(is);
        }
    }

    public DmiGaleWarningParser(File file) throws FileNotFoundException {
        this(new FileInputStream(file));
    }

    public GaleWarning parse() throws IOException {
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

    private GaleWarning parseGaleWarnings(String input) throws IOException {
        GaleWarning result = new GaleWarning();

        BufferedReader reader = new BufferedReader(new StringReader(input));

        String date = reader.readLine();
        date = prettifyDateText(date);
        DateTimeFormatter formatter = DateTimeFormat.forPattern("EEEE dd MMMM yyyy HH:mm").withZone(DateTimeZone.UTC)
                .withLocale(DEFAULT_LOCALE);
        DateTime ts = formatter.parseDateTime(date);
        result.setFrom(ts.toDate());

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().length() != 0) {
                if (line.indexOf("Varsel nummer") == 0) {
                    result.setNumber(Integer.valueOf(line.substring(14)));
                } else if (line.indexOf("Kulingvarsel") != 0) {
                    String value = reader.readLine();
                    value = value.replace(" m/s.", ".");
                    value = value.replace("m/s.", ".");
                    value = value.replace(".", " m/s.");
                    String name = line.replace(":", "");
                    result.getDistricts().put(name, value);
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
            throw new IOException("Expected exactly one <" + elementName + "> element within <" + root.getNodeName()
                    + "> element");
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
