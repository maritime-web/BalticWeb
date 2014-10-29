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

package dk.dma.embryo.tiles.service;

import dk.dma.embryo.tiles.json.JsonTileSet;
import dk.dma.embryo.tiles.model.TileSet;
import org.joda.time.DateTime;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jesper Tejlgaard on 10/02/14
 */

public class TileSetTest {


    @Test
    public void testToJsonModel() {
        // Test data
        DateTime ts1 = DateTime.now();
        DateTime ts2 = DateTime.now();
        List<TileSet> tileSets = new ArrayList<>();
        TileSet tileSet1 = new TileSet("name1", "provider1", "area1", "sourceType", TileSet.Status.CONVERTING, ts1, "type1");
        tileSet1.setUrl("url1");
        tileSets.add(tileSet1);
        tileSets.add(new TileSet("name2", "provider2", "areaY", "sourceType", TileSet.Status.SUCCESS, ts2, "type2"));
        tileSets.add(new TileSet("name3", "provider3", "areaX", "sourceType", null, null, "type1"));

        // Execution
        List<JsonTileSet> result = TileSet.toJsonModel(tileSets);

        // Expectation
        List<JsonTileSet> expected = new ArrayList<>();
        expected.add(new JsonTileSet("name1", "provider1", "area1", "sourceType", ts1.toDate(), "url1"));
        expected.add(new JsonTileSet("name2", "provider2", "areaY", "sourceType", ts2.toDate(), null));
        expected.add(new JsonTileSet("name3", "provider3", "areaX", "sourceType", null, null));

        ReflectionAssert.assertReflectionEquals(expected, result);
    }
}
