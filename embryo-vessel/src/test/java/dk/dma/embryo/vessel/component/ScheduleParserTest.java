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
package dk.dma.embryo.vessel.component;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import org.jglue.cdiunit.CdiRunner;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.unitils.reflectionassert.ReflectionAssert;

import dk.dma.embryo.vessel.json.ScheduleResponse;
import dk.dma.embryo.vessel.json.Voyage;

/**
 * @author Jesper Tejlgaard
 */
@RunWith(CdiRunner.class)
public class ScheduleParserTest {

    private DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-yyyy HH:mm").withZoneUTC();

    @Inject
    private ScheduleParser parser;

    @Test
    public void testUploadWithMandatoryColumns() throws IOException {
//        DateTime lastDepartureDate = formatter.parseDateTime("12-9-2014 12:00");

        InputStream is = getClass().getResourceAsStream("/schedule-upload/scheduleUploadMandatoryColumns.xls");
        ScheduleResponse response = parser.parse(is);

        ScheduleResponse expected = new ScheduleResponse();
        expected.setVoyages(new Voyage[3]);
        expected.getVoyages()[0] = new Voyage(null, "Nuuk", null, null, formatter.parseDateTime("19-09-2014 13:00").toDate(), formatter.parseDateTime("19-09-2014 17:00").toDate(), null, null, null);
        expected.getVoyages()[1] = new Voyage(null, "Upernavik", null, null, formatter.parseDateTime("20-09-2014 09:00").toDate(), formatter.parseDateTime("20-09-2014 13:00").toDate(), null, null, null);
        expected.getVoyages()[2] = new Voyage(null, "Ilulissat", null, null, formatter.parseDateTime("20-09-2014 15:00").toDate(), formatter.parseDateTime("20-09-2014 18:00").toDate(), null, null, null);
        expected.setErrors(new String[0]);

        ReflectionAssert.assertReflectionEquals(expected, response);
    }

    @Test
    public void testUploadWithAllColumns() throws IOException {
//        DateTime lastDepartureDate = formatter.parseDateTime("12-9-2014 12:00");

        InputStream is = getClass().getResourceAsStream("/schedule-upload/scheduleUploadAllColumns.xls");
        ScheduleResponse response = parser.parse(is);

        ScheduleResponse expected = new ScheduleResponse();
        expected.setVoyages(new Voyage[3]);
        expected.getVoyages()[0] = new Voyage("OWDD-2014-32-1", "Nuuk", null, null, formatter.parseDateTime("19-09-2014 13:00").toDate(), formatter.parseDateTime("19-09-2014 17:00").toDate(), 10 , 10, Boolean.TRUE);
        expected.getVoyages()[1] = new Voyage("OWDD-2014-32-2", "Upernavik", null, null, formatter.parseDateTime("20-09-2014 09:00").toDate(), formatter.parseDateTime("20-09-2014 13:00").toDate(), 12, 15, Boolean.TRUE);
        expected.getVoyages()[2] = new Voyage("OWDD-2014-32-3", "Ilulissat", null, null, formatter.parseDateTime("20-09-2014 15:00").toDate(), formatter.parseDateTime("20-09-2014 18:00").toDate(), 12, 12,  Boolean.FALSE);
        expected.setErrors(new String[0]);

        ReflectionAssert.assertReflectionEquals(expected, response);
    }

}
