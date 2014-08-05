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
package dk.dma.embryo.vessel.json.client;

import java.util.Map;

import javax.inject.Inject;

import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import dk.dma.embryo.common.configuration.PropertyFileService;

@RunWith(CdiRunner.class)
@AdditionalClasses(value = { AisJsonClientFactory.class, PropertyFileService.class })
public class AisViewServiceIT {
    @Inject
    FullAisViewService fullAisViewService;

    @Inject
    LimitedAisViewService limitedAisViewService;

    @Test
    public void test() {
        AisViewService.VesselListResult fullList = fullAisViewService.vesselList(0);
        System.out.println("Full list: " + fullList.getVesselsInWorld());

        AisViewService.VesselListResult limitedList = limitedAisViewService.vesselList(0);
        System.out.println("Limited list: " + limitedList.getVesselsInWorld());
    }

    @Test
    public void testDetails() {
        Map<String, Object> details = fullAisViewService.vesselTargetDetails(220443000, 0);
        System.out.println("Details: " + details);
    }

    @Test
    public void testHistory() {
        Map<String, Object> details = limitedAisViewService.vesselTargetDetails(434253250, 1);

        System.out.println("Details with History: " + details);
//        System.out.println(((Map) details.get("pastTrack")).get("points").getClass());
//        List list = (List) ((Map) details.get("pastTrack")).get("points");
//        System.out.println(list.get(0).getClass());
//        System.out.println(new ArrayList(((Map) list.get(0)).keySet()).get(0).getClass());
//        System.out.println(new ArrayList(((Map) list.get(0)).values()).get(0).getClass());
    }
}
