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
import static dk.dma.embryo.vessel.json.client.AisClientHelper.WITH_EXACT_EARTH;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class AisClientHelperTest {

    @Test
    public void testSourceFilterWithExactEarth() {
        
        /* 
         * ExactEarth = 802
         * orbcomm = 808
         * This is an exclude filter so we are only excluding arbcomm and keeping ExactEarth
         */
        String sourceFilterWithExactEarth = AisClientHelper.getSourceFilter(WITH_EXACT_EARTH);
        
        Assert.assertEquals("s.region != 808", sourceFilterWithExactEarth);
    }

    @Test
    public void testSourceFilterWithoutExactEarth() {
        
        /* 
         * ExactEarth = 802
         * orbcomm = 808
         * This is an exclude filter so we are only excluding arbcomm and keeping ExactEarth
         */
        String sourceFilterWitoutExactearth = AisClientHelper.getSourceFilter(WITHOUT_EXACT_EARTH);

        Assert.assertEquals("s.region != 802,808", sourceFilterWitoutExactearth);
    }
    
    @Test
    public void testMergeAisVesselsWithArcticVessels() {
        
        List<Vessel> listeEt = new ArrayList<Vessel>();
        
        listeEt.add(createVesselObject(1L, "ET"));
        listeEt.add(createVesselObject(2L, "TO"));
        listeEt.add(createVesselObject(4L, "FIRE"));
        
        List<Vessel> listeTo = new ArrayList<Vessel>();
        listeTo.add(createVesselObject(1L, "ET_NY"));
        listeTo.add(createVesselObject(3L, "TRE"));
        listeTo.add(createVesselObject(4L, "FIRE"));
        
        List<Vessel> mergedList = AisClientHelper.mergeAisVesselsWithArcticVessels(listeEt, listeTo);
        
        Assert.assertEquals(4, mergedList.size());
        
        for (Vessel vessel : mergedList) {
            System.out.println(vessel.getName());
        }
    
    }
    
    @Test
    public void testGetMmsiAsSeparatedList() {
        
        List<dk.dma.embryo.vessel.model.Vessel> listeEt = new ArrayList<dk.dma.embryo.vessel.model.Vessel>();
        
        listeEt.add(createVesselModelObject(1L));
        listeEt.add(createVesselModelObject(2L));
        listeEt.add(createVesselModelObject(4L));
        
        String mmsiAsSeparatedList = AisClientHelper.getMmsiAsSeparatedList(listeEt);
        String[] split = mmsiAsSeparatedList.split(",");
        
        Assert.assertEquals(3, split.length);
        Assert.assertTrue("String does not contain 1!", mmsiAsSeparatedList.contains("1"));
        Assert.assertTrue("String does not contain 2!", mmsiAsSeparatedList.contains("2"));
        Assert.assertTrue("String does not contain 3!", mmsiAsSeparatedList.contains("4"));
        
    }
    
    private Vessel createVesselObject(Long mmsi, String name) {
        
        Vessel vessel = new Vessel();
        vessel.setMmsi(mmsi);
        vessel.setName(name);
        
        return vessel;
    }
    
    private dk.dma.embryo.vessel.model.Vessel createVesselModelObject(Long mmsi) {
        
        dk.dma.embryo.vessel.model.Vessel vessel = new dk.dma.embryo.vessel.model.Vessel();
        vessel.setMmsi(mmsi);
        
        return vessel;
    }
}
