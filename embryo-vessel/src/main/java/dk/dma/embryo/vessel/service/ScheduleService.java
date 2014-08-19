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
package dk.dma.embryo.vessel.service;

import java.util.List;

import javax.ejb.Local;

import dk.dma.embryo.vessel.model.Route;
import dk.dma.embryo.vessel.model.Voyage;

@Local
public interface ScheduleService {

    void updateSchedule(Long mmsi, List<Voyage> toBeSaved, String[] toDelete);

    List<Voyage> getSchedule(Long mmsi);

    String saveRoute(Route route, String voyageId, Boolean active);

    String saveRoute(Route route);

    Route getActiveRoute(Long mmsi);

    Route activateRoute(String routeEnavId, Boolean activate);

    Route getRouteByEnavId(String enavId);
}
