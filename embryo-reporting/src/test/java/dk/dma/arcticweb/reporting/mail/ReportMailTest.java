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
package dk.dma.arcticweb.reporting.mail;

import javax.inject.Inject;

import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import dk.dma.arcticweb.reporting.model.GreenPosDeviationReport;
import dk.dma.arcticweb.reporting.model.GreenPosFinalReport;
import dk.dma.arcticweb.reporting.model.GreenPosPositionReport;
import dk.dma.arcticweb.reporting.model.GreenPosSailingPlanReport;
import dk.dma.arcticweb.reporting.model.ReportedRoute;
import dk.dma.arcticweb.reporting.model.ReportedWayPoint;
import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.common.util.DateTimeConverter;
import dk.dma.embryo.vessel.model.Position;

/**
 * @author Jesper Tejlgaard
 */
@RunWith(CdiRunner.class)
@AdditionalClasses(value = { PropertyFileService.class })
public class ReportMailTest {

    @Inject
    PropertyFileService propertyFileService;

    @Test
    public void testSendDeviationReport_noWaypoints() throws Exception {
        // TEST DATA
        GreenPosDeviationReport report = new GreenPosDeviationReport("MyVessel", 12L, "callsign", new Position(10.0,
                10.0), "My Deviation Description");
        report.setTs(DateTimeConverter.getDateTimeConverter("MM").toObject("01-01-2014 12:00:34"));

        // EXECUTE
        ReportMail mail = new ReportMail(report, "test@test.dk", propertyFileService).build();

        // VERIFY
        String header = "ArcticWeb Greenpos Deviation Report from MyVessel";
        String body = "A (Vessel): MyVessel/callsign MMSI 12\n";
        body += "B (Report time): 01-01-2014 12:00:34 UTC\n";
        body += "C (Position): 10 00.000N 010 00.000E\n";
        body += "L (Deviation): My Deviation Description\n";
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
    public void testSendDeviationReport_withWaypoints() throws Exception {
        // TEST DATA
        GreenPosDeviationReport report = new GreenPosDeviationReport("MyVessel", 12L, "callsign", new Position(10.0,
                10.0), "My Deviation Description");
        report.setTs(DateTimeConverter.getDateTimeConverter("MM").toObject("01-01-2014 12:00:34"));
        ReportedRoute route = new ReportedRoute("mykey", "myroute");
        route.addWayPoint(new ReportedWayPoint("wp1", 10.0, 10.0));
        route.addWayPoint(new ReportedWayPoint("wp2", 12.0, 12.0));
        report.setRoute(route);

        // EXECUTE
        ReportMail mail = new ReportMail(report, "test@test.dk", propertyFileService).build();

        // VERIFY
        String header = "ArcticWeb Greenpos Deviation Report from MyVessel";
        String body = "A (Vessel): MyVessel/callsign MMSI 12\n";
        body += "B (Report time): 01-01-2014 12:00:34 UTC\n";
        body += "C (Position): 10 00.000N 010 00.000E\n";
        body += "L (Deviation): My Deviation Description\n";
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
    public void testSendSailingPlanReport_withRouteData() throws Exception {

        // TEST DATA
        DateTime eta = DateTimeConverter.getDateTimeConverter("MS").toObject("02-02-2014 12:00");
        GreenPosSailingPlanReport report = new GreenPosSailingPlanReport("MyVessel", 12L, "callsign", new Position(
                10.0, 10.0), "My Weather", "My Ice", 1.0, 230, "Nuuk", eta, 12, "My Route Description");
        report.setTs(DateTimeConverter.getDateTimeConverter("MM").toObject("01-02-2014 14:01:25"));
        ReportedRoute route = new ReportedRoute("mykey", "myroute");
        route.addWayPoint(new ReportedWayPoint("wp1", 10.0, 10.0));
        route.addWayPoint(new ReportedWayPoint("wp2", 12.0, 12.0));
        report.setRoute(route);

        // EXECUTE
        ReportMail mail = new ReportMail(report, "test@test.dk", propertyFileService).build();

        // VERIFY
        String header = "ArcticWeb Greenpos Sailing Plan Report from MyVessel";
        String body = "A (Vessel): MyVessel/callsign MMSI 12\n";
        body += "B (Report time): 01-02-2014 14:01:25 UTC\n";
        body += "C (Position): 10 00.000N 010 00.000E\n";
        body += "E (Course): 230\n";
        body += "F (Speed): 1.0\n";
        body += "I (Destination & ETA): Nuuk, 02-02-2014 12:00 UTC\n";
        body += "X (Persons on Board): 12\n";
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
                10.0, 10.0), "My Weather", "My Ice", 1.0, 230, "Nuuk", eta, 12, null);
        report.setTs(DateTimeConverter.getDateTimeConverter("MM").toObject("01-02-2014 14:01:25"));

        // EXECUTE
        ReportMail mail = new ReportMail(report, "test@test.dk", propertyFileService).build();

        // VERIFY
        String header = "ArcticWeb Greenpos Sailing Plan Report from MyVessel";
        String body = "A (Vessel): MyVessel/callsign MMSI 12\n";
        body += "B (Report time): 01-02-2014 14:01:25 UTC\n";
        body += "C (Position): 10 00.000N 010 00.000E\n";
        body += "E (Course): 230\n";
        body += "F (Speed): 1.0\n";
        body += "I (Destination & ETA): Nuuk, 02-02-2014 12:00 UTC\n";
        body += "X (Persons on Board): 12\n";
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
        GreenPosFinalReport report = new GreenPosFinalReport("MyVessel", 12L, "callsign", new Position(10.0, 10.0),
                "My Weather", "My Ice");
        report.setTs(DateTimeConverter.getDateTimeConverter("MM").toObject("01-02-2014 14:01:25"));

        // EXECUTE
        ReportMail mail = new ReportMail(report, "test@test.dk", propertyFileService).build();

        // VERIFY
        String header = "ArcticWeb Greenpos Final Report from MyVessel";
        String body = "A (Vessel): MyVessel/callsign MMSI 12\n";
        body += "B (Report time): 01-02-2014 14:01:25 UTC\n";
        body += "C (Position): 10 00.000N 010 00.000E\n";
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
                new Position(10.0, 10.0), "My Weather", "My Ice", 2.0, 134);
        report.setTs(DateTimeConverter.getDateTimeConverter("MM").toObject("01-02-2014 06:03:25"));

        // EXECUTE
        ReportMail mail = new ReportMail(report, "test@test.dk", propertyFileService).build();

        // VERIFY
        String header = "ArcticWeb Greenpos Position Report from MyVessel";
        String body = "A (Vessel): MyVessel/callsign MMSI 12\n";
        body += "B (Report time): 01-02-2014 06:03:25 UTC\n";
        body += "C (Position): 10 00.000N 010 00.000E\n";
        body += "E (Course): 134\n";
        body += "F (Speed): 2.0\n";
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

}
