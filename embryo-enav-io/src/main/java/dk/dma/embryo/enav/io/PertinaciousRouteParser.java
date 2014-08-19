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
package dk.dma.embryo.enav.io;

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
