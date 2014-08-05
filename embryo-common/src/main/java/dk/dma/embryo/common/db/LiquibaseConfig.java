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
package dk.dma.embryo.common.db;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import dk.dma.embryo.common.configuration.PropertiesReader;


/**
 * @author Jesper Tejlgaard
 */
public class LiquibaseConfig {
    
    private Properties properties;
    
    public LiquibaseConfig() throws IOException, URISyntaxException{
        properties = new PropertiesReader().read();
    }
    
    public String getChangeLog(){
        return properties.getProperty("embryo.liquibase.changelog");
    }
    
    public Map<String, String> getParameters(){
        return new HashMap<>();
    }
    
    public boolean isEnabled(){
        return "true".equals(properties.getProperty("embryo.liquibase.enabled"));
    }

    public boolean isDropFirst(){
        return "true".equals(properties.getProperty("embryo.liquibase.dropFirst"));
    }
    
    public String getDefaultSchema(){
        return properties.getProperty("embryo.liquibase.defaultSchema");
    }

    public String getContexts(){
        return properties.getProperty("embryo.liquibase.contexts");
    }
    
    
}
