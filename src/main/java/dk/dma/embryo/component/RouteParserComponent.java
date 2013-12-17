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
package dk.dma.embryo.component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import dk.dma.embryo.domain.Route;
import dk.dma.enav.serialization.RouteParser;

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
