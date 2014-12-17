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

        String recipientName = propertyFileService.getProperty("embryo.notification.mail.name." + recipient);
        String reportType = propertyFileService.getProperty("embryo.notification.mail.mailName." + recipient);

        environment.put("ReportType", reportType);
        environment.put("Recipient", recipientName);
        environment.put("VesselName", report.getVesselName());
        environment.put("VesselMmsi", "" + report.getVesselMmsi());
        environment.put("VesselCallSign", report.getVesselCallSign());
        environment.put("Number", report.getNumber().toString());
        environment.put("ReportTS", reportTsConverter.toString(report.getTs()));
        environment.put("Latitude", report.getPosition().getLatitudeAsString());
        environment.put("Longitude", report.getPosition().getLongitudeAsString());
        environment.put("MalFunctions", report.getVesselMalFunctions());

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
        setFrom(propertyFileService.getProperty("embryo.notification.mail.from"));
        setCc(userEmail);

        return this;
    }
}
