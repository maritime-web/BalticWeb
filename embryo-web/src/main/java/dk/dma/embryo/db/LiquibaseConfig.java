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
package dk.dma.embryo.db;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import dk.dma.embryo.configuration.PropertiesReader;

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
