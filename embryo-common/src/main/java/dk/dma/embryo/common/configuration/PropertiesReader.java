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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Tejlgaard
 */
public class PropertiesReader {
    private final Logger logger = LoggerFactory.getLogger(PropertiesReader.class);

    private static String DEFAULT_CONFIGURATION_RESOURCE_NAME = "/default-configuration.properties";

    public Properties read() throws IOException, URISyntaxException {
        Properties properties = new Properties();
        
        if (getClass().getResourceAsStream(DEFAULT_CONFIGURATION_RESOURCE_NAME) != null) {
            properties.load(getClass().getResourceAsStream(DEFAULT_CONFIGURATION_RESOURCE_NAME));
        }
        
        String externalConfigurationSystemProperty = properties.getProperty(
                "propertyFileService.externalConfigurationSystemProperty", "configuration");

        if (System.getProperty(externalConfigurationSystemProperty) != null) {
            logger.info("Reading configuration from: " + System.getProperty(externalConfigurationSystemProperty));
            FileInputStream fis = new FileInputStream(new File(new URI(
                    System.getProperty(externalConfigurationSystemProperty))));
            properties.load(fis);
        } else {
            logger.info("System property " + externalConfigurationSystemProperty
                    + " is not set. Not reading external configuration.");
        }
        
        return properties;
    }
}
