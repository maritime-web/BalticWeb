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
package dk.dma.embryo.dataformats;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import javax.inject.Inject;

import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import dk.dma.embryo.common.configuration.LogConfiguration;
import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.configuration.PropertyFileService;

/**
 * Testing various configurations in satellite-default-configuration.properties as errors would otherwise first show
 * up in test and production environments.
 * 
 * @author Jesper Tejlgaard
 */

@RunWith(CdiRunner.class)
@AdditionalClasses({ PropertyFileService.class, LogConfiguration.class })
public class DataFormatsDefaultConfigurationTest {

    @Inject
    @Property(value = "embryo.iceberg.dmi.regions")
    Map<String, String> defaultIcebergRegions;

    @Test
    public void testDefaultIcebergRegions() {
        assertEquals("Cape Farewell", defaultIcebergRegions.get("CapeFarewell_Icebergs"));
        assertEquals("Central West", defaultIcebergRegions.get("CentralWest_Icebergs"));
        assertEquals("North East", defaultIcebergRegions.get("NorthEast_Icebergs"));
        assertEquals("North West", defaultIcebergRegions.get("NorthWest_Icebergs"));
        assertEquals("Qaanaaq", defaultIcebergRegions.get("Qaanaaq_Icebergs"));
        assertEquals("South East", defaultIcebergRegions.get("SouthEast_Icebergs"));
        assertEquals("South West", defaultIcebergRegions.get("SouthWest_Icebergs"));
        assertEquals("North", defaultIcebergRegions.get("North_Icebergs"));
        assertEquals("Central East", defaultIcebergRegions.get("CentralEast_Icebergs"));
        assertEquals("North and Central East", defaultIcebergRegions.get("NorthAndCentralEast_Icebergs"));
        assertEquals(10, defaultIcebergRegions.size());
    }

}
