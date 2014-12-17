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
package dk.dma.embryo.dataformats.inshore;

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
