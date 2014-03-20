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
package dk.dma.arcticweb.filetransfer;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;

import dk.dma.arcticweb.filetransfer.ShapeFileMeasurerJob.NamedTimeStamps;

/**
 * @author Jesper Tejlgaard
 */
public class ShapeFileMeasurerJobTest {

    @Test
    public void testNamedTimeStamps() {

        NamedTimeStamps ts = new NamedTimeStamps();

        DateTime now = DateTime.now(DateTimeZone.UTC);

        ts.add("first", now.minusHours(1));
        ts.add("second", now.minusHours(2));
        ts.add("third", now.minusDays(1));
        ts.add("fourth", now.minusDays(2));

        ts.clearOldThanMinutes(60*24);

        Assert.assertTrue(ts.contains("first"));
        Assert.assertTrue(ts.contains("second"));
    }

}
