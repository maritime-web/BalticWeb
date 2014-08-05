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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
        
        InputStream defaultStream = getClass().getResourceAsStream(DEFAULT_CONFIGURATION_RESOURCE_NAME);
        if (defaultStream != null) {
            Properties temp = new Properties();
            temp.load(defaultStream);
        
            String moduleDefaults = temp.getProperty("propertyFileService.moduleDefaultConfigurations");
            if(moduleDefaults!= null && moduleDefaults.trim().length() > 0){
                for(String moduleDefault : moduleDefaults.split(",")){
                    InputStream is = getClass().getResourceAsStream(moduleDefault + ".properties");
                    if(is == null){
                        logger.info("Could not find property file {}");
                    }else{
                        properties.load(is);
                    }
                }
            }

            // Add properties from main default configuration (and possibly overwrite properties from module properties)
            properties.putAll(temp);
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
