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

import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.net.ftp.FTPFile;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Predicate;

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
    }

    @Test
    public void testValidFormat_InvalidDateFormats() {
        Predicate<FTPFile> predicate = DmiInshoreIceReportPredicates.validFormat();
        FTPFile file = new FTPFile();

        file.setName("2014-01-30_v2.txt");
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

}
