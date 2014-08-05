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
import java.util.Date;

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
import javax.ejb.Singleton;
import javax.inject.Inject;

import dk.dma.embryo.common.util.ParseUtils;
import dk.dma.embryo.vessel.json.ScheduleResponse;

/**
 * @author Jacob Avlund
 */

@Singleton
public class ScheduleParserComponent {
    
    @Inject
    private ScheduleParser scheduleParser;

    public ScheduleResponse parseSchedule(InputStream stream, String lastDeparture) throws IOException {
        Date lastDepartureDate = null;
        if(lastDeparture == null || lastDeparture.isEmpty()) {
            lastDepartureDate = new Date(0);
        } else {
            Long lastDepartureLong = ParseUtils.parseLong(lastDeparture);
            lastDepartureDate = new Date(lastDepartureLong);
        }

        ScheduleResponse response = scheduleParser.parse(stream, lastDepartureDate);

        return response;
    }

}
