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
package dk.dma.embryo.common.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Jesper Tejlgaard
 */
public class NamedTimeStampsTest {

    @Test
    public void testNamedTimeStamps() {

        NamedtimeStamps ts = new NamedtimeStamps();

        DateTime now = DateTime.now(DateTimeZone.UTC);

        ts.add("first", now.minusHours(1));
        ts.add("second", now.minusHours(2));
        ts.add("third", now.minusDays(1).minusMinutes(1));
        ts.add("fourth", now.minusDays(2));

        ts.clearOldThanMinutes(60 * 24);

        Assert.assertTrue(ts.contains("first"));
        Assert.assertTrue(ts.contains("second"));
        Assert.assertFalse(ts.contains("third"));
        Assert.assertFalse(ts.contains("fourth"));
    }

}
