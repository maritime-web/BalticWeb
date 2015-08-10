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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Jesper Tejlgaard
 */
public class PropertyFileServiceTest {


    private Properties properties;

    @Before
    public void setup() {
        properties = new Properties();
    }

    @Test
    public void testGetMapProperty_Value() throws IOException, URISyntaxException {
        properties.put("mapProperty", "key1=foo bar;key2=foo baz;key3=foo.b!=3,2");

        PropertyFileService service = new PropertyFileService(properties);

        // verify non existing property
        Map<String, String> map = service.getMapProperty("notAvailable");
        Assert.assertNotNull(map);
        Assert.assertEquals(0, map.size());

        // verify present map property
        map = service.getMapProperty("mapProperty");
        Assert.assertNotNull(map);
        Assert.assertEquals(3, map.size());

        Assert.assertEquals("foo bar", map.get("key1"));
        Assert.assertEquals("foo baz", map.get("key2"));
        Assert.assertEquals("foo.b!=3,2", map.get("key3"));

    }

    @Test
    public void testGetProviderProperty() throws IOException, URISyntaxException {
        properties.put("embryo.providers", "prov1,prov2");
        properties.put("embryo.providers.prov1.shortName", "P1");
        properties.put("embryo.providers.prov1.types", "t1,t2");
        properties.put("embryo.providers.prov1.types.t1.localDirectory", "{user.home}/arcticweb/t1");
        properties.put("embryo.providers.prov1.types.t2.localDirectory", "{user.home}/arcticweb/t2");
        properties.put("embryo.providers.prov2.types", "TT");
        properties.put("embryo.providers.prov2.types.TT.name", "TtTt");
        properties.put("embryo.providers.prov2.types.TT.localDirectory", "{user.home}/arcticweb/TeTe");

        PropertyFileService service = new PropertyFileService(properties);
        List<Provider> providers = service.getProvidersProperty("embryo.providers");

        assertNotNull(providers);
        assertEquals(2, providers.size());

        Provider provider = providers.get(0);
        assertEquals("P1", provider.getShortName());

        List<Type> types = provider.getTypes();
        assertNotNull(types);
        assertEquals(2, types.size());

        Type type = types.get(0);
        assertEquals("t1", type.getName());
        assertNotNull(type.getLocalDirectory());
        assertTrue(type.getLocalDirectory().endsWith("/arcticweb/t1"));
        assertFalse(type.getLocalDirectory().contains("{user.home}"));

        type = types.get(1);
        assertEquals("t2", type.getName());
        assertNotNull(type.getLocalDirectory());
        assertTrue(type.getLocalDirectory().endsWith("/arcticweb/t2"));
        assertFalse(type.getLocalDirectory().contains("{user.home}"));

        provider = providers.get(1);
        assertEquals("prov2", provider.getShortName());

        types = provider.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());

        type = types.get(0);
        assertEquals("TtTt", type.getName());
        assertNotNull(type.getLocalDirectory());
        assertTrue(type.getLocalDirectory().endsWith("/arcticweb/TeTe"));
        assertFalse(type.getLocalDirectory().contains("{user.home}"));
    }

}
