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

import dk.dma.arcticweb.reporting.model.GreenPosDMIReport;
import dk.dma.arcticweb.reporting.model.GreenPosDeviationReport;
import dk.dma.arcticweb.reporting.model.GreenPosFinalReport;
import dk.dma.arcticweb.reporting.model.GreenPosPositionReport;
import dk.dma.arcticweb.reporting.model.GreenPosReport;
import dk.dma.arcticweb.reporting.model.GreenPosSailingPlanReport;
import dk.dma.arcticweb.reporting.model.ReportedRoute;
import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.common.mail.Mail;
import dk.dma.embryo.common.util.DateTimeConverter;

/**
 * @author Jesper Tejlgaard
 */
public class ReportMail extends Mail<ReportMail> {

    private final GreenPosReport report;
    private final String userEmail;
    private final String recipient;

    public ReportMail(GreenPosReport report, String userEmail, String recipient, PropertyFileService propertyFileService) {
        super(propertyFileService);
        this.report = report;
        this.userEmail = userEmail;
        this.recipient = recipient;
    }

    public ReportMail build() {
        DateTimeConverter reportTsConverter = DateTimeConverter.getDateTimeConverter("MM");

        environment.put("VesselName", report.getVesselName());
        environment.put("VesselMmsi", "" + report.getVesselMmsi());
        environment.put("VesselCallSign", report.getVesselCallSign());
        environment.put("ReportTS", reportTsConverter.toString(report.getTs()));
        environment.put("Latitude", report.getPosition().getLatitudeAsString());
        environment.put("Longitude", report.getPosition().getLongitudeAsString());

        template("greenposReport");

        if (report instanceof GreenPosDMIReport) {
            environment.put("IceInformation", ((GreenPosDMIReport) report).getIceInformation());
            environment.put("Weather", ((GreenPosDMIReport) report).getWeather());
        }

        if (report instanceof GreenPosPositionReport) {
            template("greenposPositionReport");
            environment.put("Course", "" + ((GreenPosPositionReport) report).getCourse());
            environment.put("Speed", "" + ((GreenPosPositionReport) report).getSpeed());
        }
        if (report instanceof GreenPosSailingPlanReport) {
            DateTimeConverter converter = DateTimeConverter.getDateTimeConverter("MS");
            GreenPosSailingPlanReport spReport = (GreenPosSailingPlanReport) report;
            String eta = converter.toString(spReport.getEtaOfArrival());
            ReportedRoute route = spReport.getRoute();

            environment.put("RouteDescription", spReport.getRouteDescription());
            environment.put("RouteWayPoints", route == null ? null : route.getWayPointsAsString());
            environment.put("Destination", spReport.getDestination());
            environment.put("PersonsOnBoard", "" + spReport.getPersonsOnBoard());
            environment.put("EtaOfArrival", eta == null ? "" : eta + " UTC");
            template("greenposSailingPlanReport");
        }
        if (report instanceof GreenPosFinalReport) {
            template("greenposFinalReport");
        }
        if (report instanceof GreenPosDeviationReport) {
            GreenPosDeviationReport dReport = (GreenPosDeviationReport) report;
            ReportedRoute route = dReport.getRoute();
            environment.put("Deviation", dReport.getDeviation());
            environment.put("RouteWayPoints", route == null ? null : route.getWayPointsAsString());
            template("greenposDeviationReport");
        }

        setTo(propertyFileService.getProperty("embryo.notification.mail.to." + recipient));
        setFrom(userEmail);
        setCc(userEmail);

        return this;
    }
}
