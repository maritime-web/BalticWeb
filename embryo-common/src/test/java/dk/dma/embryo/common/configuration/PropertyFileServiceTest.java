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
