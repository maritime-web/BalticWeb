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

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.vessel.json.client.AisViewServiceAllAisData.HistoricalTrack;
import dk.dma.embryo.vessel.json.client.AisViewServiceAllAisData.Vessel;

@RunWith(CdiRunner.class)
@AdditionalClasses(value = { AisJsonClientFactory.class, PropertyFileService.class })
public class AisViewServiceIT {
    
    @Inject
    AisViewServiceAllAisData aisViewServiceAllAisData;
    
    @Inject
    FullAisViewService fullAisViewService;

    @Test
    public void test() {
        
        List<Vessel> vesselList = aisViewServiceAllAisData.vesselList(AisViewServiceAllAisData.LOOK_BACK_PT24H, AisViewServiceAllAisData.LOOK_BACK_PT24H);
        System.out.println("Full list: " + vesselList.size());
    }

    @Test
    public void testDetails() {
        Map<String, Object> details = fullAisViewService.vesselTargetDetails(220443000, 0);
        System.out.println("Details: " + details);
    }

    @Test
    public void testHistory() {
        
        List<HistoricalTrack> historicalTrack = aisViewServiceAllAisData.historicalTrack(220443000, 500, AisViewServiceAllAisData.LOOK_BACK_PT12H);
        System.out.println("Details with History: " + historicalTrack.size());
    }
}
