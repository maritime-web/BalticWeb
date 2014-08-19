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
package dk.dma.embryo.msi;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import dk.dma.embryo.common.log.EmbryoLogService;


public class MsiClientIT {

    private EmbryoLogService logService;
    
    @Before
    public void setup(){
        logService = Mockito.mock(EmbryoLogService.class);
    }
    
    @Test
    public void test() {
        
        MsiClientImpl msiClientImpl = new MsiClientImpl(logService);

        msiClientImpl.endpoint = "http://msi-beta.e-navigation.net/msi/ws/warning";
        msiClientImpl.regions = "GL,DK";

        msiClientImpl.init();

        for (MsiClient.MsiItem msiItem : msiClientImpl.getActiveWarnings(null)) {
            System.out.println(""+msiItem);
        }
    }
}
