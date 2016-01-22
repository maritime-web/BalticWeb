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

import dk.dma.embryo.vessel.json.ScheduleResponse;
import dk.dma.embryo.vessel.json.Voyage;
import dk.dma.embryo.vessel.model.Berth;
import dk.dma.embryo.vessel.model.Vessel;
import dk.dma.embryo.vessel.persistence.GeographicDao;
import dk.dma.embryo.vessel.persistence.ScheduleDao;
import org.jglue.cdiunit.CdiRunner;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.unitils.reflectionassert.ReflectionAssert;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jesper Tejlgaard
 */
@RunWith(CdiRunner.class)
public class ScheduleUploadPostProcessorTest {

    private DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-yyyy HH:mm").withZoneUTC();

    @Produces
    @Mock
    private ScheduleDao scheduleDao;

    @Produces
    @Mock
    private GeographicDao geographicDao;

    @Inject
    private ScheduleParser parser;

    @Inject
    private ScheduleUploadPostProcessor scheduleUploadPostProcessor;

    @Before
    public void setup() {
        List<Berth> nuukResult = new ArrayList<>();
        nuukResult.add(new Berth("Nuuk", null, "64 10.400N", "051 43.500W"));
        Mockito.when(geographicDao.lookup("Nuuk")).thenReturn(nuukResult);

        List<Berth> upernavikResult = new ArrayList<>();
        upernavikResult.add(new Berth("Upernavik", null, "72 47.500N", "056 09.400W"));
//        upernavikResult.add(new Berth("Upernavik Kujallek", "Søndre Upernavik", "72 09.200N", "055 32.000W"));
        Mockito.when(geographicDao.lookup("Upernavik")).thenReturn(upernavikResult);

        List<Berth> ilulissatResult = new ArrayList<>();
        ilulissatResult.add(new Berth("Ilulissat", "Jacobshavn", "69 13.500N", "051 06.000W"));
        Mockito.when(geographicDao.lookup("Ilulissat")).thenReturn(ilulissatResult);
    }

    @Test
    public void testUploadWithMandatoryColumns() throws IOException {
        DateTime lastDepartureDate = formatter.parseDateTime("12-9-2014 12:00");

        InputStream is = getClass().getResourceAsStream("/schedule-upload/scheduleUploadMandatoryColumns.xls");
        ScheduleResponse response = parser.parse(is);
        response = scheduleUploadPostProcessor.validate(response, 12345678L, lastDepartureDate.toDate());

        ScheduleResponse expected = new ScheduleResponse();
        expected.setVoyages(new Voyage[3]);
        expected.getVoyages()[0] = new Voyage(null, "Nuuk", 64.17333333333333, -51.725, formatter.parseDateTime("19-09-2014 13:00").toDate(), formatter.parseDateTime("19-09-2014 17:00").toDate(), null, null, null);
        expected.getVoyages()[1] = new Voyage(null, "Upernavik", 72.79166666666667, -56.156666666666666, formatter.parseDateTime("20-09-2014 09:00").toDate(), formatter.parseDateTime("20-09-2014 13:00").toDate(), null, null, null);
        expected.getVoyages()[2] = new Voyage(null, "Ilulissat", 69.225, -51.1, formatter.parseDateTime("20-09-2014 15:00").toDate(), formatter.parseDateTime("20-09-2014 18:00").toDate(), null, null, null);
        expected.setErrors(new String[0]);

        ReflectionAssert.assertReflectionEquals(expected, response);
    }

    @Test
    public void testUploadWithLocationMultiMatch() throws IOException {
        List<Berth> ilulissatResult = new ArrayList<>();
        ilulissatResult.add(new Berth("Aappilattoq", null, "72 53.000N", "055 36.600W"));
        ilulissatResult.add(new Berth("Aappilattoq", null, "60 09.600N", "044 17.200W"));
        Mockito.when(geographicDao.lookup("Aappilattoq")).thenReturn(ilulissatResult);

        DateTime lastDepartureDate = formatter.parseDateTime("12-9-2014 12:00");

        InputStream is = getClass().getResourceAsStream("/schedule-upload/scheduleUploadLocationMultiMatch.xls");
        ScheduleResponse response = parser.parse(is);
        response = scheduleUploadPostProcessor.validate(response, 12345678L, lastDepartureDate.toDate());

        ScheduleResponse expected = new ScheduleResponse();
        expected.setVoyages(new Voyage[4]);
        expected.getVoyages()[0] = new Voyage(null, "Nuuk", 64.17333333333333, -51.725, formatter.parseDateTime("19-09-2014 13:00").toDate(), formatter.parseDateTime("19-09-2014 17:00").toDate(), null, null, null);
        expected.getVoyages()[1] = new Voyage(null, "Upernavik", 72.79166666666667, -56.156666666666666, formatter.parseDateTime("20-09-2014 09:00").toDate(), formatter.parseDateTime("20-09-2014 13:00").toDate(), null, null, null);
        expected.getVoyages()[2] = new Voyage(null, "Ilulissat", 69.225, -51.1, formatter.parseDateTime("20-09-2014 15:00").toDate(), formatter.parseDateTime("20-09-2014 18:00").toDate(), null, null, null);
        expected.getVoyages()[3] = new Voyage(null, "Aappilattoq", null, null, formatter.parseDateTime("21-09-2014 09:00").toDate(), formatter.parseDateTime("21-09-2014 12:00").toDate(), null, null, null);
        expected.setErrors(new String[]{"1 locations could not be found, please add them manually."});

        ReflectionAssert.assertReflectionEquals(expected, response);
    }

    @Test
    public void testUploadWithAllColumns() throws IOException {
        Mockito.when(scheduleDao.getVoyageByEnavId(Mockito.anyString())).thenReturn(null);

        DateTime lastDepartureDate = formatter.parseDateTime("12-9-2014 12:00");

        InputStream is = getClass().getResourceAsStream("/schedule-upload/scheduleUploadAllColumns.xls");
        ScheduleResponse response = parser.parse(is);
        response = scheduleUploadPostProcessor.validate(response, 12345678L, lastDepartureDate.toDate());

        ScheduleResponse expected = new ScheduleResponse();
        expected.setVoyages(new Voyage[3]);
        expected.getVoyages()[0] = new Voyage("OWDD-2014-32-1", "Nuuk", 64.17333333333333, -51.725, formatter.parseDateTime("19-09-2014 13:00").toDate(), formatter.parseDateTime("19-09-2014 17:00").toDate(), 10 , 10, Boolean.TRUE);
        expected.getVoyages()[1] = new Voyage("OWDD-2014-32-2", "Upernavik", 72.79166666666667, -56.156666666666666, formatter.parseDateTime("20-09-2014 09:00").toDate(), formatter.parseDateTime("20-09-2014 13:00").toDate(), 12, 15, Boolean.TRUE);
        expected.getVoyages()[2] = new Voyage("OWDD-2014-32-3", "Ilulissat", 69.225, -51.1, formatter.parseDateTime("20-09-2014 15:00").toDate(), formatter.parseDateTime("20-09-2014 18:00").toDate(), 12, 12,  Boolean.FALSE);
        expected.setErrors(new String[0]);

        ReflectionAssert.assertReflectionEquals(expected, response);
    }

    @Test
    public void locationFinder_testVoyageIdByOtherVessel() throws IOException {
        Vessel vessel = new Vessel(1111L);
        dk.dma.embryo.vessel.model.Voyage voyage1 = new dk.dma.embryo.vessel.model.Voyage("Nuuk", "64 10.400N", "051 43.500W", null, null);
        voyage1.setEnavId("2014-32-1");
        vessel.addVoyageEntry(voyage1);
        dk.dma.embryo.vessel.model.Voyage voyage2 = new dk.dma.embryo.vessel.model.Voyage("Upernavik", "72 47.500N", "056 09.400W", null, null);
        voyage2.setEnavId("2014-32-2");
        vessel.addVoyageEntry(voyage2);

        Vessel myVessel = new Vessel(2222L);
        dk.dma.embryo.vessel.model.Voyage voyage3 = new dk.dma.embryo.vessel.model.Voyage("Ilulissat", "64 10.400N", "051 43.500W", null, null);
        voyage3.setEnavId("OWDD-2014-32-3");
        myVessel.addVoyageEntry(voyage3);


        Mockito.when(scheduleDao.getVoyageByEnavId("2014-32-1")).thenReturn(voyage1);
        Mockito.when(scheduleDao.getVoyageByEnavId("2014-32-2")).thenReturn(voyage2);
        Mockito.when(scheduleDao.getVoyageByEnavId("OWDD-2014-32-3")).thenReturn(voyage3);

        ScheduleUploadPostProcessor.VoyageFilter voyageFinder = new ScheduleUploadPostProcessor.VoyageFilter(scheduleDao, 2222L);

        Assert.assertFalse(voyageFinder.test(new Voyage("2014-32-1", "Nuuk", null, null, null, null, null, null, null)));
        Assert.assertFalse(voyageFinder.test(new Voyage("2014-32-2", "Upernavik", null, null, null, null, null, null, null)));
        Assert.assertTrue(voyageFinder.test(new Voyage("OWDD-2014-32-3", "Ilulissat", null, null, null, null, null, null, null)));
        Assert.assertTrue(voyageFinder.test(new Voyage("2014-32-4", "Første gang", null, null, null, null, null, null, null)));
        Assert.assertFalse(voyageFinder.test(new Voyage("2014-32-4", "Anden gang", null, null, null, null, null, null, null)));
        Assert.assertFalse(voyageFinder.test(new Voyage("2014-32-2", "Upernavik", null, null, null, null, null, null, null)));
        Assert.assertFalse(voyageFinder.test(new Voyage("2014-32-4", "Tredje gang", null, null, null, null, null, null, null)));

        List<String> expectedErrors = new ArrayList<>();
        expectedErrors.add("Please assign unique Id values in Excel before uploading. The following id(s) are used in schedule(s) for other vessel(s): 2014-32-1, 2014-32-2");
        expectedErrors.add("Id 2014-32-4 discovered more than once. Only using data for first occurrence.");
        ReflectionAssert.assertReflectionEquals(expectedErrors, voyageFinder.getIdErrors());
    }


}
