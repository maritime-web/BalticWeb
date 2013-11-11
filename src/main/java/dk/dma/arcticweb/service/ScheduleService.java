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
package dk.dma.arcticweb.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;

import dk.dma.embryo.domain.Route;
import dk.dma.embryo.domain.Schedule;
import dk.dma.embryo.domain.Voyage;

@Local
public interface ScheduleService {

    void updateSchedule(Long mmsi, List<Voyage> toBeSaved, String[] toDelete);

    List<Voyage> getSchedule(Long mmsi);

    String saveRoute(Route route, String voyageId, Boolean active);

    String saveRoute(Route route);

    Route getActiveRoute(Long mmsi);

    Route activateRoute(String routeEnavId, Boolean activate);

    Route getRouteByEnavId(String enavId);

    Route parseRoute(String fileName, InputStream is, Map<String, String> context) throws IOException;
}
