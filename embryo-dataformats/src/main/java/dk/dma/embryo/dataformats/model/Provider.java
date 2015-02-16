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
package dk.dma.embryo.dataformats.model;

/**
 * The Class Provider.
 *
 * @author Jesper Tejlgaard
 */
public class Provider {
    private String key;
    private String name;
    private String shortName;

    public Provider(String key, String name, String shortName) {
        super();
        this.key = key;
        this.name = name;
        this.shortName = shortName;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null)        ? 0 : key.hashCode());
        result = prime * result + ((name == null)       ? 0 : name.hashCode());
        result = prime * result + ((shortName == null)  ? 0 : shortName.hashCode());
        
        return result;
    }
    
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }

    public String getShortName() {
        return shortName;
    }
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
