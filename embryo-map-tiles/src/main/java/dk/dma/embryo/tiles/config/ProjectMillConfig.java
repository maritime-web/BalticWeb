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

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by Jesper Tejlgaard on 8/21/14.
 */
public class ProjectMillConfig {

    private File savedTo;

    private final Map<String, Object> config;

    public ProjectMillConfig(final Map<String, Object> config) {
        this.config = config;
    }

    public ProjectMillConfig save(File destination) {
        if (!destination.getName().endsWith(".json")) {
            throw new IllegalArgumentException("File should have extension .json");
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(destination, config);
            savedTo = destination;
            return this;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ProjectMillConfig execute(String projectmillDirectory, String tilemillDirectory) throws IOException {
        if (savedTo != null) {
            throw new IllegalStateException("No file name for configuration. Can not execute unknown configuration.");
        }

        TileMillExecutor executor = new TileMillExecutor(projectmillDirectory, tilemillDirectory);
        executor.execute(savedTo);
        return this;
    }

    public static enum Format {
        PNG, PDF, SVG, MBTILES;

        public String toString() {
            return name().toLowerCase();
        }
    }
}
