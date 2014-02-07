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
package dk.dma.arcticweb.filetransfer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * @author Jesper Tejlgaard
 */
public class HttpContentFilesReader {

    private final Pattern pattern;

    public HttpContentFilesReader(String regExAcceptanceFilter) {
        pattern = Pattern.compile(regExAcceptanceFilter);
    }

    public HttpContentFilesReader() {
        this("(.*)(\\.dbf|\\.prj|\\.shp|\\.shx|\\.zip)");
    }

    public List<String> readFiles(Document doc) throws IOException {
        List<String> files = new ArrayList<>();
        for (Element link : doc.select("a")) {
            if (link.hasAttr("href") && valueAccepted(link.attr("href"))) {
                files.add(link.attr("href"));
            }
        }

        return files;
    }

    private boolean valueAccepted(String fileName) {
        boolean result = pattern.matcher(fileName).matches();
        return result;
    }

}
