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

package dk.dma.embryo.tiles.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jesper Tejlgaard on 8/21/14.
 */
public class ProjectMillConfigBuilder {

    private Map<String, Object> config = new HashMap<>();

    public ProjectMillConfig build() {
        return new ProjectMillConfig(config);
    }

    public ProjectMillConfigBuilder setSource(String sourceProject) {
        config.put("source", sourceProject);
        return this;
    }

    public ProjectMillConfigBuilder setDestination(String destinationProject) {
        config.put("destination", destinationProject);
        return this;
    }

    public ProjectMillConfigBuilder setFormat(ProjectMillConfig.Format format) {
        config.put("format", format.toString());
        return this;
    }

    public ProjectMillConfigBuilder setMinzoom(Integer minzoom) {
        config.put("minzoom", minzoom);
        return this;
    }

    public ProjectMillConfigBuilder setMaxzoom(Integer maxzoom) {
        config.put("maxzoom", maxzoom);
        return this;
    }

    public ProjectMillConfigBuilder setProjectBounds(Double d1, Double d2, Double d3, Double d4) {
        getProject().put("bounds", new Double[]{d1, d2, d3, d4});
        return this;
    }

    /**
     * Assumes only one pre configured layer.
     */
    public ProjectMillConfigBuilder setProjectLayerDataSource(String datasource) {
        HashMap<String, Object> project = getProject();

        HashMap<String, Object>[] layers = (HashMap<String, Object>[]) project.get("Layer");
        if (layers == null || layers.length == 0) {
            layers = new HashMap[]{new HashMap<String, Object>()};
            project.put("Layer", layers);
        } else if (layers.length > 1) {
            throw new IllegalStateException("Can only call this method if config contains 0 or 1 layers. ");
        }

        HashMap<String, Object> ds = new HashMap<>();
        ds.put("file", datasource);

        layers[0].put("Datasource", ds);
        return this;
    }


    private HashMap<String, Object> getProject() {
        if (!config.containsKey("mml")) {
            config.put("mml", new HashMap<String, Object>());
        }
        return (HashMap<String, Object>) config.get("mml");
    }
}
