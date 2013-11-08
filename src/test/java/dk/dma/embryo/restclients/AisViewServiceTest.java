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

import dk.dma.configuration.PropertyFileService;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Map;

@RunWith(CdiRunner.class)
@AdditionalClasses(value = {RestClientFactory.class, PropertyFileService.class})
public class AisViewServiceTest {
    @Inject
    FullAisViewService fullAisViewService;

    @Inject
    LimitedAisViewService limitedAisViewService;

    @Ignore
    @Test
    public void test() {
        AisViewService.VesselListResult fullList = fullAisViewService.vesselList(0);
        System.out.println("Full list: " + fullList.getVesselsInWorld());

        AisViewService.VesselListResult limitedList = limitedAisViewService.vesselList(0);
        System.out.println("Limited list: " + limitedList.getVesselsInWorld());
    }

    @Ignore
    @Test
    public void testDetails() {
        Map details = fullAisViewService.vesselTargetDetails(220443000, 0);
        System.out.println("Details: "+details);
    }
}
