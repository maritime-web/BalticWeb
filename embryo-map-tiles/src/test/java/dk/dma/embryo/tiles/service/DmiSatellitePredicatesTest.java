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

import com.google.common.base.Predicate;
import org.apache.commons.net.ftp.FTPFile;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Jesper Tejlgaard
 */
public class DmiSatellitePredicatesTest {

    private static DateTime youngerThan;

    @BeforeClass
    public static void staticSetup() {
        youngerThan = DateTimeFormat.forPattern("yyyy-MM-dd").parseLocalDate("2014-10-30").toDateTimeAtStartOfDay(DateTimeZone.UTC);
    }

    @Test
    public void testDateIsAfter() {
        Predicate<FTPFile> predicate = DmiSatellitePredicates.dateIsAfter(youngerThan);

        FTPFile file = new FTPFile();

        file.setName("201410300001.XXX");
        Assert.assertTrue(predicate.apply(file));

        file.setName("20141030-yyyy");
        Assert.assertFalse(predicate.apply(file));

        file.setName("20141031-yyyy");
        Assert.assertTrue(predicate.apply(file));

        file.setName("20141101_xxx");
        Assert.assertTrue(predicate.apply(file));

        file.setName("20141029_xxx");
        Assert.assertFalse(predicate.apply(file));
    }


}
