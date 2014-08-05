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
