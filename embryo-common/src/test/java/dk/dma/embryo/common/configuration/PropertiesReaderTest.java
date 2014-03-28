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
