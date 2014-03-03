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
package dk.dma.embryo.restclients;

import java.util.Date;

import javax.inject.Inject;

import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import dk.dma.embryo.configuration.PropertyFileService;

@RunWith(CdiRunner.class)
@AdditionalClasses(value = {RestClientFactory.class, PropertyFileService.class})
public class DmiSejlRuteServiceIT {
    @Inject
    DmiSejlRuteService dmiSejlRuteService;

    @Test
    public void test() {
        DmiSejlRuteService.SejlRuteRequest request = new DmiSejlRuteService.SejlRuteRequest();
        request.setMssi(999999999);
        request.setDatatypes(new String[]{"sealevel", "current", "wave", "wind", "density"});
        request.setDt(15);

        DmiSejlRuteService.Waypoint wp1 = new DmiSejlRuteService.Waypoint();
        wp1.setEta(DmiSejlRuteService.DATE_FORMAT.format(new Date(System.currentTimeMillis() + 1000L * 3600 * 0)));
        wp1.setHeading("RL");
        wp1.setLat(55.70816666666666);
        wp1.setLon(12.3115);

        DmiSejlRuteService.Waypoint wp2 = new DmiSejlRuteService.Waypoint();
        wp2.setEta(DmiSejlRuteService.DATE_FORMAT.format(new Date(System.currentTimeMillis() + 1000L * 3600 * 3)));
        wp2.setHeading("RL");
        wp2.setLat(55.725183333333334);
        wp2.setLon(12.648666666666667);
        request.setWaypoints(new DmiSejlRuteService.Waypoint[]{wp1, wp2});

        System.out.println(request);
        
        DmiSejlRuteService.SejlRuteResponse sejlRuteResponse = dmiSejlRuteService.sejlRute(request);
        System.out.println("" + sejlRuteResponse);
    }
}
