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
 * Testing various configurations in dataformats-default-configuration.properties as errors would otherwise first show
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
