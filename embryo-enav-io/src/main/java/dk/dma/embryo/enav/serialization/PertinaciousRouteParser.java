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
package dk.dma.embryo.enav.serialization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import dk.dma.enav.model.voyage.Route;

/**
 * Utility class for loading routes in different file formats.
 * 
 * This class is not thread safe. Nor should it be reused.
 * 
 * @author Jesper Tejlgaard
 */
public class PertinaciousRouteParser extends RouteParser {

    // private static final Logger LOG = LoggerFactory.getLogger(RouteLoader.class);

    private BufferedReader reader;
    
    public PertinaciousRouteParser(Reader reader) {
        if (reader instanceof BufferedReader) {
            this.reader = (BufferedReader) reader;
        } else {
            this.reader = new BufferedReader(reader);
        }
    }

    public PertinaciousRouteParser(File file) throws FileNotFoundException {
        this(new FileReader(file));
    }

    private static final String FORMAT_ERR_MSG = "Error in route format.";

    public Route parse() throws IOException {
        Route route = null;
        try {
            route = new SimpleRouteParser(reader).parse();
        } catch (IOException e) {
//            try {
////                route = loadRou(file, navSettings);
//            } catch (IOException e1) {
//                try {
////                    route = loadRt3(file, navSettings);
//                } catch (IOException e2) {
//                }
//            }
        }

        if (route == null) {
            throw new IOException("Route file could no be recognized as any readable format");
        }

        return route;
    }    
}
