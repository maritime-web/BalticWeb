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
import org.unitils.reflectionassert.ReflectionAssert;

import java.util.HashMap;
import java.util.Map;

import static dk.dma.embryo.tiles.config.ProjectMillConfig.Format.MBTILES;

/**
 * Created by Jesper Tejlgaard on 8/21/14.
 */
public class ProjectMillConfigBuilderTest {

    @Test
    public void testBuild() {
        ProjectMillConfigBuilder builder = new ProjectMillConfigBuilder();
        builder.setSource("foo").setDestination("bar").setFormat(MBTILES).setMinzoom(2).setMaxzoom(12);
        builder.setProjectBounds(-10.0, -20.0, 20.0, 10.0);
        builder.setProjectLayerDataSource("file://source.tif");

        //Expected
        HashMap<String, Object> config = new HashMap<String, Object>();
        config.put("source", "foo");
        config.put("destination", "bar");
        config.put("format", "mbtiles");
        config.put("minzoom", 2);
        config.put("maxzoom", 12);

        Map<String, Object> mml = new HashMap<>();
        config.put("mml", mml);
        mml.put("bounds", new Double[]{-10.0, -20.0, 20.0, 10.0});
        Map<String, Object> layer = new HashMap<>();
        mml.put("Layer", new Map[]{layer});
        Map<String, Object> datasource = new HashMap<>();
        layer.put("Datasource", datasource);
        datasource.put("file", "file://source.tif");

        ProjectMillConfig expected = new ProjectMillConfig(config);

        ReflectionAssert.assertReflectionEquals(expected, builder.build());
    }
}
