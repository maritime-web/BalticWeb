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
        Long lastDepartureLong = ParseUtils.parseLong(lastDeparture);
        Date lastDepartureDate = new Date(lastDepartureLong);

        ScheduleResponse response = scheduleParser.parse(stream, lastDepartureDate);

        return response;
    }

}
