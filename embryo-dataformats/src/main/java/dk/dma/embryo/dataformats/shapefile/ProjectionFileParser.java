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
package dk.dma.embryo.dataformats.shapefile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ProjectionFileParser {
    public static String parse(InputStream is) throws IOException {
        try {
            String projection = "EPSG:4326";
            String data = "";

            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            while (br.ready()) {
                data += br.readLine() + "\n";
            }

            if (data.contains("Google Maps Global Mercator")) {
                projection = "GOOGLE_MERCATOR";
            }

            return projection;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
}
