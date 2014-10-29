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

package dk.dma.embryo.tiles.tiles;

import dk.dma.embryo.tiles.config.ProjectMillConfig;
import dk.dma.embryo.tiles.config.ProjectMillConfigBuilder;
import org.junit.Test;

import java.io.File;

import static dk.dma.embryo.tiles.config.ProjectMillConfig.Format.MBTILES;

/**
 * Created by Jesper Tejlgaard on 8/21/14.
 */
public class ProjectMillConfigTest {

    @Test
    public void testSave() {
        ProjectMillConfigBuilder builder = new ProjectMillConfigBuilder();
        builder.setSource("foo").setDestination("bar").setFormat(MBTILES).setMinzoom(2).setMaxzoom(12);
        builder.setProjectBounds(-10.0, -20.0, 20.0, 10.0);
        builder.setProjectLayerDataSource("file://source.tif");
        ProjectMillConfig config = builder.build();

        String currentDir = getClass().getResource("/.").getFile();
        File directory = new File(new File(currentDir).getParent(), "tmp");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        System.out.println(directory);

        File destination = new File(directory, "test.config.json");
        config.save(destination);


    }
}
