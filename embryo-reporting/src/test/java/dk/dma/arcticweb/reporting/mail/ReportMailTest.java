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
package dk.dma.arcticweb.reporting.mail;

import dk.dma.arcticweb.reporting.model.GreenPosDeviationReport;
import dk.dma.arcticweb.reporting.model.GreenPosFinalReport;
import dk.dma.arcticweb.reporting.model.GreenPosPositionReport;
import dk.dma.arcticweb.reporting.model.GreenPosSailingPlanReport;
import dk.dma.arcticweb.reporting.model.ReportedRoute;
import dk.dma.arcticweb.reporting.model.ReportedWayPoint;
import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.common.util.DateTimeConverter;
import dk.dma.embryo.vessel.model.Position;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

/**
 * @author Jesper Tejlgaard
 */
@RunWith(CdiRunner.class)
@AdditionalClasses(value = {PropertyFileService.class})
public class ReportMailTest {

    @Inject
    PropertyFileService propertyFileService;

    @Test
    public void testSendDeviationReport_noWaypoints() throws Exception {
        // TEST DATA
        GreenPosDeviationReport report = new GreenPosDeviationReport("MyVessel", 12L, "callsign", new Position(10.0,
                10.0), 4, "My Deviation Description", null);
        report.setTs(DateTimeConverter.getDateTimeConverter("MM").toObject("01-01-2014 12:00:34"));

        // EXECUTE
        ReportMail mail = new ReportMail(report, "test@test.dk", "greenpos", propertyFileService).build();

        // VERIFY
        String header = "ArcticWeb Greenpos Deviation Report from MyVessel";
        String body = "GREENPOS - DR 4\n";
        body += "A (Vessel): MyVessel/callsign MMSI 12\n";
        body += "B (Report time): 01-01-2014 12:00:34 UTC\n";
        body += "C (Position): 10 00.000N 010 00.000E\n";
        body += "L (Deviation): My Deviation Description\n";
        body += "L (Route WayPoints): -\n";
        body += "Q (Malfunctions): -\n";
        body += "\n";
        body += "Reported via ArcticWeb.";

        Assert.assertEquals("arktiskcom@gmail.com", mail.getTo());
        Assert.assertEquals("noreply@dma.dk", mail.getFrom());
        Assert.assertEquals("test@test.dk", mail.getCc());
        Assert.assertEquals(header, mail.getHeader());
        Assert.assertEquals(body, mail.getBody());
    }

    @Test
    public void testSendDeviationReport_withWaypoints() throws Exception {
        // TEST DATA
        GreenPosDeviationReport report = new GreenPosDeviationReport("MyVessel", 12L, "callsign", new Position(10.0,
                10.0), 4, "My Deviation Description", "Starboard hole");
        report.setTs(DateTimeConverter.getDateTimeConverter("MM").toObject("01-01-2014 12:00:34"));
        ReportedRoute route = new ReportedRoute("mykey", "myroute");
        route.addWayPoint(new ReportedWayPoint("wp1", 10.0, 10.0));
        route.addWayPoint(new ReportedWayPoint("wp2", 12.0, 12.0));
        report.setRoute(route);

        // EXECUTE
        ReportMail mail = new ReportMail(report, "test@test.dk", "greenpos", propertyFileService).build();

        // VERIFY
        String header = "ArcticWeb Greenpos Deviation Report from MyVessel";
        String body = "GREENPOS - DR 4\n";
        body += "A (Vessel): MyVessel/callsign MMSI 12\n";
        body += "B (Report time): 01-01-2014 12:00:34 UTC\n";
        body += "C (Position): 10 00.000N 010 00.000E\n";
        body += "L (Deviation): My Deviation Description\n";
        body += "L (Route WayPoints): [10 00.000N,010 00.000E],  [12 00.000N,012 00.000E]\n";
        body += "Q (Malfunctions): Starboard hole\n";
        body += "\n";
        body += "Reported via ArcticWeb.";

        Assert.assertEquals("arktiskcom@gmail.com", mail.getTo());
        Assert.assertEquals("noreply@dma.dk", mail.getFrom());
        Assert.assertEquals("test@test.dk", mail.getCc());
        Assert.assertEquals(header, mail.getHeader());
        Assert.assertEquals(body, mail.getBody());
    }

    @Test
    public void testSendSailingPlanReport_withRouteData() throws Exception {

        // TEST DATA
        DateTime eta = DateTimeConverter.getDateTimeConverter("MS").toObject("02-02-2014 12:00");
        GreenPosSailingPlanReport report = new GreenPosSailingPlanReport("MyVessel", 12L, "callsign", new Position(
                10.0, 10.0), 1, "My Weather", "My Ice", 1.0, 230, "Nuuk", eta, 12, "My Route Description", null);
        report.setTs(DateTimeConverter.getDateTimeConverter("MM").toObject("01-02-2014 14:01:25"));
        ReportedRoute route = new ReportedRoute("mykey", "myroute");
        route.addWayPoint(new ReportedWayPoint("wp1", 10.0, 10.0));
        route.addWayPoint(new ReportedWayPoint("wp2", 12.0, 12.0));
        report.setRoute(route);

        // EXECUTE
        ReportMail mail = new ReportMail(report, "test@test.dk", "greenpos", propertyFileService).build();

        // VERIFY
        String header = "ArcticWeb Greenpos Sailing Plan Report from MyVessel";
        String body = "GREENPOS - SP 1\n";
        body += "A (Vessel): MyVessel/callsign MMSI 12\n";
        body += "B (Report time): 01-02-2014 14:01:25 UTC\n";
        body += "C (Position): 10 00.000N 010 00.000E\n";
        body += "E (Course): 230\n";
        body += "F (Speed): 1.0\n";
        body += "I (Destination & ETA): Nuuk, 02-02-2014 12:00 UTC\n";
        body += "X (Persons on Board): 12\n";
        body += "Q (Malfunctions): -\n";
        body += "S (Ice): My Ice\n";
        body += "S (Weather): My Weather\n";
        body += "L (Route Description): My Route Description\n";
        body += "L (Route WayPoints): [10 00.000N,010 00.000E],  [12 00.000N,012 00.000E]\n";
        body += "\n";
        body += "Reported via ArcticWeb.";

        Assert.assertEquals("arktiskcom@gmail.com", mail.getTo());
        Assert.assertEquals("noreply@dma.dk", mail.getFrom());
        Assert.assertEquals("test@test.dk", mail.getCc());
        Assert.assertEquals(header, mail.getHeader());
        Assert.assertEquals(body, mail.getBody());
    }

    @Test
    public void testSendSailingPlanReport_noRouteData() throws Exception {
        // TEST DATA
        DateTime eta = DateTimeConverter.getDateTimeConverter("MS").toObject("02-02-2014 12:00");
        GreenPosSailingPlanReport report = new GreenPosSailingPlanReport("MyVessel", 12L, "callsign", new Position(
                10.0, 10.0), 1, "My Weather", "My Ice", 1.0, 230, "Nuuk", eta, 12, null, "bad captain");
        report.setTs(DateTimeConverter.getDateTimeConverter("MM").toObject("01-02-2014 14:01:25"));

        // EXECUTE
        ReportMail mail = new ReportMail(report, "test@test.dk", "greenpos", propertyFileService).build();

        // VERIFY
        String header = "ArcticWeb Greenpos Sailing Plan Report from MyVessel";
        String body = "GREENPOS - SP 1\n";
        body += "A (Vessel): MyVessel/callsign MMSI 12\n";
        body += "B (Report time): 01-02-2014 14:01:25 UTC\n";
        body += "C (Position): 10 00.000N 010 00.000E\n";
        body += "E (Course): 230\n";
        body += "F (Speed): 1.0\n";
        body += "I (Destination & ETA): Nuuk, 02-02-2014 12:00 UTC\n";
        body += "X (Persons on Board): 12\n";
        body += "Q (Malfunctions): bad captain\n";
        body += "S (Ice): My Ice\n";
        body += "S (Weather): My Weather\n";
        body += "L (Route Description): -\n";
        body += "L (Route WayPoints): -\n";
        body += "\n";
        body += "Reported via ArcticWeb.";

        Assert.assertEquals("arktiskcom@gmail.com", mail.getTo());
        Assert.assertEquals("noreply@dma.dk", mail.getFrom());
        Assert.assertEquals("test@test.dk", mail.getCc());
        Assert.assertEquals(header, mail.getHeader());
        Assert.assertEquals(body, mail.getBody());
    }

    @Test
    public void testSendFinalReport() throws Exception {

        // TEST DATA
        GreenPosFinalReport report = new GreenPosFinalReport("MyVessel", 12L, "callsign", new Position(10.0, 10.0), 5,
                "My Weather", "My Ice", null);
        report.setTs(DateTimeConverter.getDateTimeConverter("MM").toObject("01-02-2014 14:01:25"));

        // EXECUTE
        ReportMail mail = new ReportMail(report, "test@test.dk", "greenpos", propertyFileService).build();

        // VERIFY
        String header = "ArcticWeb Greenpos Final Report from MyVessel";
        String body = "GREENPOS - FR 5\n";
        body += "A (Vessel): MyVessel/callsign MMSI 12\n";
        body += "B (Report time): 01-02-2014 14:01:25 UTC\n";
        body += "C (Position): 10 00.000N 010 00.000E\n";
        body += "Q (Malfunctions): -\n";
        body += "S (Ice): My Ice\n";
        body += "S (Weather): My Weather\n";
        body += "\n";
        body += "Reported via ArcticWeb.";

        Assert.assertEquals("arktiskcom@gmail.com", mail.getTo());
        Assert.assertEquals("noreply@dma.dk", mail.getFrom());
        Assert.assertEquals("test@test.dk", mail.getCc());
        Assert.assertEquals(header, mail.getHeader());
        Assert.assertEquals(body, mail.getBody());
    }

    @Test
    public void testSendPositionReport() throws Exception {
        // TEST DATA
        GreenPosPositionReport report = new GreenPosPositionReport("MyVessel", 12L, "callsign",
                new Position(10.0, 10.0), 2, "My Weather", "My Ice", 2.0, 134, null);
        report.setTs(DateTimeConverter.getDateTimeConverter("MM").toObject("01-02-2014 06:03:25"));

        // EXECUTE
        ReportMail mail = new ReportMail(report, "test@test.dk", "coastalcontrol", propertyFileService).build();

        // VERIFY
        String header = "ArcticWeb Coastal Control Position Report from MyVessel";
        String body = "KYSTKONTROL - PR 2\n";
        body += "A (Vessel): MyVessel/callsign MMSI 12\n";
        body += "B (Report time): 01-02-2014 06:03:25 UTC\n";
        body += "C (Position): 10 00.000N 010 00.000E\n";
        body += "E (Course): 134\n";
        body += "F (Speed): 2.0\n";
        body += "Q (Malfunctions): -\n";
        body += "S (Ice): My Ice\n";
        body += "S (Weather): My Weather\n";
        body += "\n";
        body += "Reported via ArcticWeb.";

        Assert.assertEquals("coastal.control.arcticweb@gmail.com", mail.getTo());
        Assert.assertEquals("noreply@dma.dk", mail.getFrom());
        Assert.assertEquals("test@test.dk", mail.getCc());
        Assert.assertEquals(header, mail.getHeader());
        Assert.assertEquals(body, mail.getBody());
    }

}
