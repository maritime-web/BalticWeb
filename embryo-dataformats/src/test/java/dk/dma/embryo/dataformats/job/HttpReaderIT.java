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
import java.io.IOException;

import org.junit.Test;

import dk.dma.embryo.dataformats.job.HttpReader;

/**
 * Integration Test reading folder and files from Russian Wether Service (AARI)
 * 
 * Named HttpReaderIT instead of HttepReaderTest to prevent maven-surefire-plugin from executing it automatically
 *
 * @author Jesper Tejlgaard
 */
public class HttpReaderIT {

    @Test
    public void testReadContent() throws IOException {
        HttpReader reader = new HttpReader("http", "wdc.aari.ru", 10000);

        System.out.println(reader.readContent("datasets/d0004/chu/sigrid/2014/"));
    }

    @Test
    public void testGetFile() throws IOException {
        HttpReader reader = new HttpReader("http", "wdc.aari.ru", 10000);

        File dir = new File(System.getProperty("user.home") + "/arcticweb/tmp");
        dir.mkdirs();
        
        File file = new File(dir.getAbsoluteFile(), "" + Math.random());
        
        reader.getFile("datasets/d0004/chu/sigrid/2014", "aari_chu_20140107_pl_a.dbf", file);
    }
}
