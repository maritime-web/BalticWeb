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


import dk.dma.embryo.common.configuration.LogConfiguration;
import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.vessel.integration.AisStoreClient.TrackPosition;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Jesper Tejlgaard
 */
@RunWith(CdiRunner.class)
@AdditionalClasses(value = {AisClientFactory.class, PropertyFileService.class, LogConfiguration.class})
public class AisStoreClientIT {

    @Inject
    private AisStoreClient aisTrackClient;

    @Inject
    @Property("embryo.aisstore.server.url")
    private String aisTrackUrl;

    @Inject
    @Property("embryo.aisstore.server.user")
    private String aisTrackUser;

    @Test
    public void testPastTrack() {
        System.out.println(aisTrackUrl);
        System.out.println(aisTrackUser);

        List<TrackPosition> trackPositions = this.aisTrackClient.pastTrack(220443000L, "s.region!=802,808", "PT1H");

        System.out.println(trackPositions);
        Assert.assertEquals(2, trackPositions.size());
    }
}
