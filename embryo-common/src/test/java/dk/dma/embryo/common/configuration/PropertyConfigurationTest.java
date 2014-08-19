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


import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;

import javax.inject.Inject;

import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiRunner.class)
@AdditionalClasses(value = {ConfiguredClassImpl.class, PropertyFileService.class})
public class PropertyConfigurationTest {

    @Inject
    ConfiguredClass service;

    @BeforeClass
    public static void init() throws Exception {
        try {
            String name = PropertyConfigurationTest.class.getResource("/external-configuration.properties").toURI()
                    .toString();
            System.setProperty("arcticweb.configuration", name);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void test() throws URISyntaxException {
        assertEquals(42, service.getSomeNumber());
        assertEquals("Ib", service.getSomeString());
        assertEquals(System.getProperty("user.home") + "/test", service.getSomeDirectory());
    }
}
