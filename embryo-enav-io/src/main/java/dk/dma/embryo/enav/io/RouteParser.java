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
package dk.dma.embryo.enav.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import dk.dma.enav.model.voyage.Route;

/**
 * 
 * @author Jesper Tejlgaard
 * 
 */
public abstract class RouteParser {

    RouteDefaults defaults = new RouteDefaults(); 
    
    public static RouteParser getRouteParser(File file) throws FileNotFoundException {
        String ext = getExtension(file.getName());
        switch (ext) {
        case "TXT":
            return new SimpleRouteParser(file);
        case "ROU":
            return new RouRouteParser(file);
        case "RT3":
            return new Rt3RouteParser(file);
        case "ROUTE":
            return new RouteRouteParser(file);
        case "" : 
            if(file.getName().matches("map\\d{4}t")){
                return new SAMRouteParser(file);
            }
        default:
            throw new IllegalArgumentException("Unknown file extension. Known extensions are 'TXT' and 'ROU'.");
//            return new PertinaciousRouteParser(file);
        }
    }

    public static RouteParser getRouteParser(String fileName, InputStream io, Map<String, String> config) {
        String ext = getExtension(fileName);
        switch (ext) {
        case "TXT":
            return new SimpleRouteParser(io, config);
        case "ROU":
            return new RouRouteParser(io, config);
        case "RT3":
            return new Rt3RouteParser(io, config);
        case "ROUTE":
            return new RouteRouteParser(io, config);
        case "" : 
            if(fileName.matches("map\\d{4}t")){
                return new SAMRouteParser(io, config);
            }
        default:
            throw new IllegalArgumentException("Unknown file extension. Known extensions are 'TXT' and 'ROU'.");
//            return new PertinaciousRouteParser(file);
        }
    }
    
    private static String getExtension(String fileName){
        int position = fileName.lastIndexOf('.');
        if(position == -1){
            return "";
        }
        return fileName.substring(position + 1).trim().toUpperCase();        
    }
    
    public RouteParser defaults(RouteDefaults defaults){
        this.defaults = defaults;
        return this;
    }
    
    public RouteDefaults getDefaults(){
        return defaults;
    }
    
    protected String waypointName(String readValue, Integer waypointCount){
        if(readValue != null && readValue.trim().length() > 0){
            return readValue;
        }
        
        return String.format("WP_%03d", waypointCount);
    }

    public abstract Route parse() throws IOException;

}
