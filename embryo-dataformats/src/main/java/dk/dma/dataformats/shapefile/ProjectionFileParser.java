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
package dk.dma.dataformats.shapefile;

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
            is.close();
        }
    }
}
