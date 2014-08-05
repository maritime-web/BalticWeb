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
package dk.dma.embryo.dataformats.job;

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
        this("(.*)(\\.dbf|\\.prj|\\.shp|\\.shx)");
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
