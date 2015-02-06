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
package dk.dma.embryo.vessel.job;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Thomas Berg
 */
public class AisReplicatorJobDataUtilTest {

    private Double maxSpeed = 1.2;
    private Double NO_MAX_SPEED;;
    private Double maxSpeedZero = 0.0;
    private Double maxSpeedAlmostZero = 0.00101;
    private Double sog = 2.5;
    
    @Test 
    public void testMaxSpeedValueSet() {
        
        Double maxSpeedCalculated = AisReplicatorJobDataUtil.getMaxSpeed(maxSpeed, sog);
        
        Assert.assertEquals(maxSpeed, maxSpeedCalculated);
    }
    
    @Test 
    public void testMaxSpeedValueNull() {
        
        Double maxSpeedCalculated = AisReplicatorJobDataUtil.getMaxSpeed(NO_MAX_SPEED, sog);
        
        Assert.assertEquals(sog, maxSpeedCalculated);
    }
    
    @Test 
    public void testMaxSpeedValueZero() {
        
        Double maxSpeedCalculated = AisReplicatorJobDataUtil.getMaxSpeed(maxSpeedZero, sog);
        
        Assert.assertEquals(sog, maxSpeedCalculated);
    }
    
    @Test 
    public void testMaxSpeedValueAlmostZero() {
        
        Double maxSpeedCalculated = AisReplicatorJobDataUtil.getMaxSpeed(maxSpeedAlmostZero, sog);
        
        Assert.assertEquals(maxSpeedAlmostZero, maxSpeedCalculated);
    }
}
