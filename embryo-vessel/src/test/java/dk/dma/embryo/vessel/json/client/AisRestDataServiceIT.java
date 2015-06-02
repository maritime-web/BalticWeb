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

import static dk.dma.embryo.vessel.json.client.AisClientHelper.WITHOUT_EXACT_EARTH;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.TimerService;
import javax.inject.Inject;

import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.vessel.json.client.AisRestDataService.AisVessel;

@RunWith(CdiRunner.class)
@AdditionalClasses(value = { AisJsonClientFactory.class, PropertyFileService.class })
public class AisRestDataServiceIT {

    @Resource
    private TimerService service;

    @Inject
    private AisRestDataService aisRestDataService;

    @Inject
    @Property("dk.dma.embryo.restclients.aisRestBaseUrl")
    private String aisRestBaseUrl;

    @Test
    public void testVesselListByMmsis() {
        
        String url = this.aisRestBaseUrl;
        System.out.println("URL -> " + url);
        
        List<String> listOfMmsi = new ArrayList<String>();
        listOfMmsi.add("211211450");
        listOfMmsi.add("271043740");
        
        String mmsiCommaSeparated = org.apache.commons.lang.StringUtils.join(listOfMmsi, ",");
        
        System.out.println("Comma separated list of MMSI: " + mmsiCommaSeparated);
        
        List<AisVessel> vesselListByMmsis = this.aisRestDataService.vesselListByMmsis(mmsiCommaSeparated, AisClientHelper.getSourceFilter(WITHOUT_EXACT_EARTH));
        
        AisVessel firstAisVessel = vesselListByMmsis.get(0);
        Assert.assertEquals(2, vesselListByMmsis.size());
        
        // Assert source attributes
//        Assert.assertNotNull("'id' attribute not set correctly!", firstAisVessel.getSource().getId());
//        Assert.assertNotNull("'region' attribute not set correctly!", firstAisVessel.getSource().getRegion());
        
        // Assert target attributes
//        Assert.assertNotNull("'mmsi' attribute not set correctly!", firstAisVessel.getTarget().getMmsi());
//        Assert.assertNotNull("'country' attribute not set correctly!", firstAisVessel.getTarget().getCountry());
//        Assert.assertNotNull("'lastReport' attribute not set correctly!", firstAisVessel.getTarget().getLastReport());
//        Assert.assertNotNull("'created' attribute not set correctly!", firstAisVessel.getTarget().getCreated());
//        Assert.assertNotNull("'targetType' attribute not set correctly!", firstAisVessel.getTarget().getTargetType());
//        Assert.assertNotNull("'vesselStatic' attribute not set correctly!", firstAisVessel.getTarget().getVesselStatic());

        // Assert vesselStatic attributes
//        Assert.assertNotNull("'mmsi' attribute not set correctly!", firstAisVessel.getTarget().getVesselStatic().getMmsi());
//        Assert.assertNotNull("'recieved' attribute not set correctly!", firstAisVessel.getTarget().getVesselStatic().getReceived());
//        Assert.assertNotNull("'sourceTimestamp' attribute not set correctly!", firstAisVessel.getTarget().getVesselStatic().getSourceTimestamp());
//        Assert.assertNotNull("'created' attribute not set correctly!", firstAisVessel.getTarget().getVesselStatic().getCreated());
//        Assert.assertNotNull("'name' attribute not set correctly!", firstAisVessel.getTarget().getVesselStatic().getName());
//        Assert.assertNotNull("'callsign' attribute not set correctly!", firstAisVessel.getTarget().getVesselStatic().getCallsign());
//        Assert.assertNotNull("'shipType' attribute not set correctly!", firstAisVessel.getTarget().getVesselStatic().getShipType());
//        Assert.assertNotNull("'postType' attribute not set correctly!", firstAisVessel.getTarget().getVesselStatic().getPostType());
//        Assert.assertNotNull("'version' attribute not set correctly!", firstAisVessel.getTarget().getVesselStatic().getVersion());
//        Assert.assertNotNull("'dte' attribute not set correctly!", firstAisVessel.getTarget().getVesselStatic().getDte());
//        Assert.assertNotNull("'shipTypeCargo' attribute not set correctly!", firstAisVessel.getTarget().getVesselStatic().getShipTypeCargo());
//        Assert.assertNotNull("'dimensions' attribute not set correctly!", firstAisVessel.getTarget().getVesselStatic().getDimensions());

//        Assert.assertNotNull("'imoNo' attribute not set correctly!", firstAisVessel.getTarget().getVesselStatic().getImoNo());
//        Assert.assertNotNull("'destination' attribute not set correctly!", firstAisVessel.getTarget().getVesselStatic().getDestination());
//        Assert.assertNotNull("'eta' attribute not set correctly!", firstAisVessel.getTarget().getVesselStatic().getEta());
//        Assert.assertNotNull("'draught' attribute not set correctly!", firstAisVessel.getTarget().getVesselStatic().getDraught());
        
        // Assert shipTypeCargo attributes
//        Assert.assertNotNull("'shipType' attribute not set correctly!", firstAisVessel.getTarget().getVesselStatic().getShipTypeCargo().getShipType());
//        Assert.assertNotNull("'code' attribute not set correctly!", firstAisVessel.getTarget().getVesselStatic().getShipTypeCargo().getCode());
//        Assert.assertNotNull("'shapCargo' attribute not set correctly!", firstAisVessel.getTarget().getVesselStatic().getShipTypeCargo().getShipCargo());
        
        // Assert shipTypeCargo attributes
//        Assert.assertNotNull("'dimBow' attribute not set correctly!", firstAisVessel.getTarget().getVesselStatic().getDimensions().getDimBow());
//        Assert.assertNotNull("'dimStern' attribute not set correctly!", firstAisVessel.getTarget().getVesselStatic().getDimensions().getDimStern());
//        Assert.assertNotNull("'dimPort' attribute not set correctly!", firstAisVessel.getTarget().getVesselStatic().getDimensions().getDimPort());
//        Assert.assertNotNull("'dimStarboard' attribute not set correctly!", firstAisVessel.getTarget().getVesselStatic().getDimensions().getDimStarboard());
        
        System.out.println("AisVessel toString: " + firstAisVessel.toString());
    }
    
    @Test
    public void testVesselListAllAllowed() {
        
        String sourceFilter = AisClientHelper.getSourceFilter(WITHOUT_EXACT_EARTH); 
        System.out.println("sourceFilter: " +  sourceFilter);

        long timerStart = System.currentTimeMillis();
        List<AisVessel> vesselListAllAllowed = this.aisRestDataService.vesselListAllAllowed(AisRestDataService.AREA_FILTER_ALLOWED, sourceFilter);
        long timerEnd = System.currentTimeMillis();
        
        System.out.println("Number of allowed vessels: " + vesselListAllAllowed.size() + " and took " + (timerEnd-timerStart) + "ms");
    }
}
