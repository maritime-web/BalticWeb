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
package dk.dma.embryo.common.configuration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Jesper Tejlgaard
 */
public class PropertyFileServiceTest {

    
    private Properties properties;
    
    @Before 
    public void setup(){
        properties = new Properties();
    }
    
    @Test
    public void testGetMapProperty_Value() throws IOException, URISyntaxException {
        properties.put("mapProperty", "key1=foo bar;key2=foo baz");
        
        PropertyFileService service = new PropertyFileService(properties);

        // verify non existing property
        Map<String, String> map = service.getMapProperty("notAvailable");
        Assert.assertNotNull(map);
        Assert.assertEquals(0, map.size());
        
        // verify present map property
        map = service.getMapProperty("mapProperty");
        Assert.assertNotNull(map);
        Assert.assertEquals(2, map.size());

        Assert.assertEquals("foo bar", map.get("key1"));
        Assert.assertEquals("foo baz", map.get("key2"));

    }

}
