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
package dk.dma.embryo.dataformats.inshore;

import static org.junit.Assert.*;

import java.io.File;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;

import dk.dma.embryo.dataformats.inshore.InshoreIceReportServiceImpl.FileInfo;

/**
 * @author Jesper Tejlgaard
 */
public class FileInfoTransformerTest {

    DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd").withZoneUTC();

    @Test
    public void test_unVersionedFile() {

        File file = new File(getClass().getResource("/inshore-ice-reports/2014-07-10.txt").getFile());

        FileInfo info = new FileInfo();
        info.file = new File(getClass().getResource("/inshore-ice-reports/2014-07-10.txt").getFile());
        info.date = formatter.parseDateTime("2014-07-10").toDateMidnight();

        
        FileInfoTransformer transformer = new FileInfoTransformer();
        FileInfo actual = transformer.apply(file);
        
        ReflectionAssert.assertReflectionEquals(info, actual);
    }

    @Test
    public void test_versionedFile() {
        File file = new File(getClass().getResource("/inshore-ice-reports/2014-07-10_v3.txt").getFile());

        FileInfo info = new FileInfo();
        info.file = new File(getClass().getResource("/inshore-ice-reports/2014-07-10_v3.txt").getFile());
        info.date = formatter.parseDateTime("2014-07-10").toDateMidnight();
        info.version = "v3";

        
        FileInfoTransformer transformer = new FileInfoTransformer();
        FileInfo actual = transformer.apply(file);
        
        ReflectionAssert.assertReflectionEquals(info, actual);
    
    }
}
