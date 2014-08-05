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
package dk.dma.embryo.vessel.component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import dk.dma.embryo.enav.io.RouteParser;
import dk.dma.embryo.vessel.model.Route;

/**
 * @author Jesper Tejlgaard
 */

public class RouteParserComponent {

    /**
     * Also sets yourship on route
     */
    public Route parseRoute(String fileName, InputStream is, Map<String, String> context) throws IOException {
        RouteParser parser = RouteParser.getRouteParser(fileName, is, context);

        dk.dma.enav.model.voyage.Route enavRoute = parser.parse();
        Route route = Route.fromEnavModel(enavRoute);

        return route;
    }

}
