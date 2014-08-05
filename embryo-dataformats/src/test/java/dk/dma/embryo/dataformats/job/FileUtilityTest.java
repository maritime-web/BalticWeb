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
package dk.dma.embryo.dataformats.job;

import java.io.File;
import java.io.FilenameFilter;

import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;

/**
 * @author Jesper Tejlgaard
 */
public class FileUtilityTest {

    @Test
    public void test() {

        String fileName = this.getClass().getResource("/shapefiles").getFile();
        FileUtility util = new FileUtility(fileName);
        String[] files = util.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                String[] parts = name.split("\\.");
                return "world_merc".equals(parts[0]);
            }
        });

        String[] expected = {"world_merc.dbf", "world_merc.shx", "world_merc.prj", "world_merc.index", "world_merc.shp"};
        ReflectionAssert.assertLenientEquals(expected, files);

    }
}
