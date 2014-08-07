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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTPFile;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Predicate;

import dk.dma.embryo.dataformats.inshore.InshoreIceReportServiceImpl.FileInfo;

/**
 * @author Jesper Tejlgaard
 */
public class DmiInshoreIceReportPredicatesTest {

    private static LocalDate mapsYoungerThan;

    @BeforeClass
    public static void staticSetup() {
        mapsYoungerThan = DateTimeFormat.forPattern("yyyy-MM-dd").parseLocalDate("2014-07-29");
    }

    @Test
    public void testValidFormat_ValidValues() {
        Predicate<FTPFile> predicate = DmiInshoreIceReportPredicates.validFormat();

        FTPFile file = new FTPFile();

        // Valid format
        file.setName("2014-01-30.txt");
        Assert.assertTrue(predicate.apply(file));

        // Valid format with version
        file.setName("2014-01-30_v2.txt");
        Assert.assertTrue(predicate.apply(file));
}

    @Test
    public void testValidFormat_InvalidFormats() {
        Predicate<FTPFile> predicate = DmiInshoreIceReportPredicates.validFormat();
        FTPFile file = new FTPFile();

        file.setName("2014-01-30_V2.txt");
        Assert.assertFalse(predicate.apply(file));

        file.setName("20140130_v2.txt");
        Assert.assertFalse(predicate.apply(file));

        // Invalid month
        file.setName("2014-14-30.txt");
        Assert.assertFalse(predicate.apply(file));

        // Invalid days
        file.setName("2014-07-32.txt");
        Assert.assertFalse(predicate.apply(file));

        // With hour
        file.setName("2014-07-30 10.txt");
        Assert.assertFalse(predicate.apply(file));

        // With hour and minute
        file.setName("2014-07-30 10:10.txt");
        Assert.assertFalse(predicate.apply(file));
    }

    @Test
    public void testValidDateValue() {
        Predicate<FTPFile> predicate = DmiInshoreIceReportPredicates.validDateValue(mapsYoungerThan);
        FTPFile file = new FTPFile();

        file.setName("2014-07-15.txt");
        Assert.assertFalse(predicate.apply(file));

        file.setName("2014-07-29.txt");
        Assert.assertFalse(predicate.apply(file));

        file.setName("2014-07-30.txt");
        Assert.assertTrue(predicate.apply(file));

        file.setName("2014-08-30.txt");
        Assert.assertTrue(predicate.apply(file));

    }

    @Test
    public void testDateLimit() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd").withZoneUTC();
        DateMidnight dateLimit = formatter.parseDateTime("2014-07-24").toDateTime(DateTimeZone.UTC).toDateMidnight();
        
        Predicate<FileInfo> predicate = DmiInshoreIceReportPredicates.dateLimit(dateLimit);

        FileInfo info = new FileInfo();
        //info.file = new File(getClass().getResource("/inshore-ice-reports/2014-07-23.txt").getFile());

        info.date = formatter.parseDateTime("2014-07-23").toDateMidnight();
        Assert.assertFalse(predicate.apply(info));

        info.date = formatter.parseDateTime("2014-07-25").toDateMidnight();
        Assert.assertTrue(predicate.apply(info));

        info.date = formatter.parseDateTime("2014-07-30").toDateTime(DateTimeZone.UTC).toDateMidnight();
        Assert.assertTrue(predicate.apply(info));
    }

    
    @Test
    public void testFileNameAndVersion() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd").withZoneUTC();
        List<FileInfo> allFiles = new ArrayList<>();
        FileInfo first = new FileInfo();
        first.file = new File(getClass().getResource("/inshore-ice-reports/2014-07-10.txt").getFile());
        first.date = formatter.parseDateTime("2014-07-10").toDateMidnight();
        allFiles.add(first);
        
        FileInfo second = new FileInfo();
        second.file = new File(getClass().getResource("/inshore-ice-reports/2014-07-10_for-test.txt").getFile());
        second.date = formatter.parseDateTime("2014-07-10").toDateMidnight();
        second.version = "for-test";
        allFiles.add(second);

        FileInfo third = new FileInfo();
        third.file = new File(getClass().getResource("/inshore-ice-reports/2014-07-10_v3.txt").getFile());
        third.date = formatter.parseDateTime("2014-07-10").toDateMidnight();
        third.version = "v3";
        allFiles.add(third);
        
        FileInfo fourth = new FileInfo();
        fourth.file = new File(getClass().getResource("/inshore-ice-reports/2014-07-24.txt").getFile());
        fourth.date = formatter.parseDateTime("2014-07-24").toDateMidnight();
        allFiles.add(fourth);

        Predicate<FileInfo> predicate = DmiInshoreIceReportPredicates.fileInfoPredicate(allFiles);

        Assert.assertFalse(predicate.apply(first));
        Assert.assertFalse(predicate.apply(second));
        Assert.assertTrue(predicate.apply(third));
        Assert.assertTrue(predicate.apply(fourth));
    }

}
