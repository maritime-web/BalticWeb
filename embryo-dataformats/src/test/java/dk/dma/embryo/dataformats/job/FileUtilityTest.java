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
package dk.dma.embryo.dataformats.job;

import java.io.File;
import java.io.FilenameFilter;

import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;

import dk.dma.embryo.dataformats.job.FileUtility;

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
