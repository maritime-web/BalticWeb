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

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

@Singleton
public class PropertyFileService {
    private static String DEFAULT_CONFIGURATION_RESOURCE_NAME = "/default-configuration.properties";
    private static String OVERRIDING_CONFIGURATION_SYSTEM_PROPERTY_NAME = "configuration";

    private Properties properties = new Properties();

    @PostConstruct
    public void init() throws IOException, URISyntaxException {
        if (getClass().getResourceAsStream(DEFAULT_CONFIGURATION_RESOURCE_NAME) != null) {
            properties.load(getClass().getResourceAsStream(DEFAULT_CONFIGURATION_RESOURCE_NAME));
        }

        if (System.getProperty(OVERRIDING_CONFIGURATION_SYSTEM_PROPERTY_NAME) != null) {
            FileInputStream fis = new FileInputStream(
                    new File(new URI(System.getProperty(OVERRIDING_CONFIGURATION_SYSTEM_PROPERTY_NAME)))
            );
            properties.load(fis);
        }
    }

    @Produces
    @Property
    public String getStringPropertyByKey(InjectionPoint ip) {
        for (Annotation a : ip.getQualifiers()) {
            if (a instanceof Property) {
                Property property = (Property) a;
                String result;

                if (properties.getProperty(property.value()) != null) {
                    result = properties.getProperty(property.value());
                } else if (!property.defaultValue().equals("")) {
                    result = property.defaultValue();
                } else {
                    throw new RuntimeException(
                            "Property " + property.value() + " set on " + ip.getMember() + " not configured. " +
                                    "Add value in configuration or define defaultValue in annotation."
                    );
                }

                if (property.substituteSystemProperties()) {
                    for (Object key : System.getProperties().keySet()) {
                        result = result.replaceAll("\\{" + key + "\\}", System.getProperty("" + key));
                    }
                }

                return result;
            }
        }
        throw new UnknownError();
    }

    @Produces
    @Property
    public int getIntegerPropertyByKey(InjectionPoint ip) {
        return Integer.parseInt(getStringPropertyByKey(ip));
    }

    @Produces
    @Property
    public double getDoublePropertyByKey(InjectionPoint ip) {
        return Double.parseDouble(getStringPropertyByKey(ip));
    }
}
