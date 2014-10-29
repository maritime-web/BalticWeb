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

import dk.dma.embryo.tiles.config.TileMillExecutor;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by Jesper Tejlgaard on 8/21/14.
 */
public class TileMillExecutorIT {

    @Test
    public void testExecution() throws IOException {

        System.out.println("Foo " + getClass().getResource("~/Git"));

        String fileName = getClass().getResource("/projectmill.testconfig.json").getFile();
        TileMillExecutor executor = new TileMillExecutor("/home/jesper/Git/projectmill", "/usr/share/tilemill");
        executor.execute(new File(fileName));


    }


}
