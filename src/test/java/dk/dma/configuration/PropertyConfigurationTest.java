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
package dk.dma.configuration;

import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

@RunWith(CdiRunner.class)
@AdditionalClasses(value = {ConfiguredClassImpl.class, PropertyFileService.class})
public class PropertyConfigurationTest {

    @Inject
    ConfiguredClass service;

    static {
        try {
            String name = PropertyConfigurationTest.class.getResource("/configuration-test.properties").toURI().toString();
            System.setProperty("arcticweb.configuration", name);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Test
    public void test() throws URISyntaxException {
        assertEquals(42, service.getSomeNumber());
        assertEquals("Ib", service.getSomeString());
        assertEquals(System.getProperty("user.home") + "/test", service.getSomeDirectory());
    }
}
