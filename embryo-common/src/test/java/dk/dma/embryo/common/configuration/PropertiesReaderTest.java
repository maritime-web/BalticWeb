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
import java.util.Properties;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Jesper Tejlgaard
 */
public class PropertiesReaderTest {

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
    public void test() throws IOException, URISyntaxException {

        PropertiesReader reader = new PropertiesReader();
        Properties properties = reader.read();

        // VERIFY: common-default-configuration.properties
        Assert.assertEquals("false", properties.get("embryo.notification.mail.enabled"));
        Assert.assertEquals("localhost", properties.get("embryo.notification.mail.smtp.host"));

        // VERIFY: test-default-configuration.properties
        Assert.assertEquals("I am here", properties.get("embryo.test.1"));
        Assert.assertEquals("Still here", properties.get("embryo.test.2"));

        // VERIFY: default-configuration.properties
        Assert.assertEquals("overridden@test.dk", properties.get("embryo.notification.mail.from"));
        Assert.assertEquals("Main default also works", properties.get("embryo.test.3"));

        // VERIFY: External configurations
        Assert.assertEquals("Ib", properties.get("test.astringproperty"));
        Assert.assertEquals("42", properties.get("test.anintegerproperty"));
        Assert.assertEquals("{user.home}/test", properties.get("test.directory"));
    }

}
