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

import java.util.Date;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Jesper Tejlgaard
 */
public class InshoreIceReportMergedTest {

    @Test
    public void test() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd").withZoneUTC();
        InshoreIceReportMerged merged = new InshoreIceReportMerged();
        
        InshoreIceReport first = new InshoreIceReport();
        first.addHeader("never to be read");
        first.addNotification(11, "eleven");
        first.addNotification(12, "twelve");
        first.addNotification(13, "thirten");
        first.addFooter("also never to be read");
        
        Date d = formatter.parseDateTime("2014-08-04").toDateMidnight().toDate();
        merged.mergeInReport(d, "2014-08-04.txt", first);

        
        InshoreIceReport second = new InshoreIceReport();
        second.addHeader("never to be read");
        second.addNotification(12, "12");
        second.addNotification(13, "13");
        second.addNotification(14, "14");
        second.addNotification(15, "15");
        second.addFooter("also never to be read");

        Date d2 = formatter.parseDateTime("2014-08-05").toDateMidnight().toDate();
        merged.mergeInReport(d2, "2014-08-05.txt", second);

        InshoreIceReport third = new InshoreIceReport();
        third.addHeader("This header is displayed");
        third.addNotification(10, "ti");
        third.addNotification(11, "elleve");
        third.addNotification(12, "tolv");
        third.addNotification(13, "tretten");
        third.addFooter("This footer is displayed");

        Date d3 = formatter.parseDateTime("2014-08-01").toDateMidnight().toDate();
        merged.mergeInReport(d3, "2014-08-01.txt", third);

        Assert.assertEquals(d2, merged.getLatestReportDate());
        Assert.assertNotNull(merged.getHeader());
        Assert.assertEquals(2, merged.getHeader().size());
        Assert.assertEquals("Danish Meteorological Institute", merged.getHeader().get(0));
        Assert.assertEquals("Ice patrol, Narsarsuaq", merged.getHeader().get(1));

        Assert.assertEquals("ti", merged.getObservations().get(10).getText());
        Assert.assertEquals(d3, merged.getObservations().get(10).getFrom());

        Assert.assertEquals("eleven", merged.getObservations().get(11).getText());
        Assert.assertEquals(d, merged.getObservations().get(11).getFrom());

        Assert.assertEquals("12", merged.getObservations().get(12).getText());
        Assert.assertEquals(d2, merged.getObservations().get(12).getFrom());

        Assert.assertEquals("13", merged.getObservations().get(13).getText());
        Assert.assertEquals(d2, merged.getObservations().get(13).getFrom());

        Assert.assertNotNull(merged.getFooter());
        Assert.assertEquals(1, merged.getFooter().size());
        Assert.assertEquals("This footer is displayed", merged.getFooter().get(0));

    }

}
