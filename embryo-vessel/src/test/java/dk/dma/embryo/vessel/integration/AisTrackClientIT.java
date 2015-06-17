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
package dk.dma.embryo.vessel.integration;


import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.vessel.integration.AisTrackClient.AisTrack;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

/**
 * @author Jesper Tejlgaard
 */
@RunWith(CdiRunner.class)
@AdditionalClasses(value = { AisTrackClientFactory.class, PropertyFileService.class })
public class AisTrackClientIT {

    @Inject
    private AisTrackClient aisTrackClient;

    @Inject
    @Property("dk.dma.embryo.restclients.aisRestBaseUrl")
    private String aisRestBaseUrl;

    @Test
    public void testVesselsByMmsis() {
        System.out.println("URL: " + this.aisRestBaseUrl);

        List<Long> mmsiNumbers = Arrays.asList(220443000L, 220516000L);

        List<AisTrack> aisTracks = this.aisTrackClient.vesselsByMmsis(mmsiNumbers, "s.region!=802,808");

        System.out.println(aisTracks);
        Assert.assertEquals(2, aisTracks.size());
    }
}
