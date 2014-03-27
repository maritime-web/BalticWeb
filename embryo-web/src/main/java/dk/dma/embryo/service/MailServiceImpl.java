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
package dk.dma.embryo.service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.common.log.EmbryoLogService;
import dk.dma.embryo.common.util.DateTimeConverter;
import dk.dma.embryo.domain.GreenPosDMIReport;
import dk.dma.embryo.domain.GreenPosDeviationReport;
import dk.dma.embryo.domain.GreenPosFinalReport;
import dk.dma.embryo.domain.GreenPosPositionReport;
import dk.dma.embryo.domain.GreenPosReport;
import dk.dma.embryo.domain.GreenPosSailingPlanReport;
import dk.dma.embryo.domain.ReportedRoute;
import dk.dma.embryo.mail.MailSender;
import dk.dma.embryo.rest.RequestAccessRestService;
import dk.dma.embryo.security.Subject;

@Named
public class MailServiceImpl implements MailService {

    @Inject
    private Logger logger;

    @Inject
    private MailSender mailSender;

    @Inject
    private EmbryoLogService embryoLogService;

    @Inject
    @Property("embryo.notification.mail.to.greenpos")
    private String greenposToEmail;

    @Inject
    @Property("embryo.notification.mail.to.requestAccess")
    private String requestAccessToEmail;

    @Inject
    @Property("embryo.notification.mail.from")
    private String fromSystemEmail;

    @Inject
    @Property("embryo.iceChart.dmi.notification.email")
    private String dmiNotificationEmail;

    @Inject
    private PropertyFileService propertyFileService;

    @Inject
    private Subject subject;

    public MailServiceImpl() {
    }

    public MailServiceImpl(Logger logger, EmbryoLogService logService, MailSender sender, Subject subject,
            PropertyFileService propertyFileService) {
        this.logger = logger;
        this.embryoLogService = logService;
        this.mailSender = sender;
        this.propertyFileService = propertyFileService;
        this.subject = subject;
    }

    private String applyTemplate(String template, Map<String, String> environment) {
        String result = template;

        for (String key : environment.keySet()) {
            String value = environment.get(key);

            if (value == null) {
                value = "-";
            }

            value = Matcher.quoteReplacement(value);

            result = result.replaceAll("\\{" + key + "\\}", value);
        }

        return result;
    }

    public void newRequestAccess(RequestAccessRestService.SignupRequest request) {
        try {
            Map<String, String> environment = new HashMap<>();

            environment.put("PreferredLogin", request.getPreferredLogin());
            environment.put("ContactPerson", request.getContactPerson());
            environment.put("EmailAddress", request.getEmailAddress());
            environment.put("MmsiNumber", request.getMmsiNumber() != null ? ("" + request.getMmsiNumber()) : "-");

            String header = propertyFileService.getProperty("embryo.notification.template.signupRequest.header");
            String body = propertyFileService.getProperty("embryo.notification.template.signupRequest.body");

            mailSender.sendEmail(requestAccessToEmail, fromSystemEmail, applyTemplate(header, environment),
                    applyTemplate(body, environment));

            embryoLogService.info(applyTemplate(header, environment) + " sent to " + requestAccessToEmail);
        } catch (Throwable t) {
            embryoLogService.error("Error sending sign up request to " + requestAccessToEmail, t);
            throw new RuntimeException(t);
        }
    }

    public void dmiNotification(String iceChart, Throwable error) {
        try {
            Map<String, String> environment = new HashMap<>();

            String msg = error.getMessage();
            if (msg.contains("Expected to read")) {
                msg = "Possible corrupt ice chart. You may want to delete the ice chart.";
            }

            environment.put("IceChart", iceChart);
            environment.put("Message", msg);
            environment.put("Error", error.getMessage());

            String header = propertyFileService.getProperty("embryo.notification.template.icechartImportError.header");
            String body = propertyFileService.getProperty("embryo.notification.template.icechartImportError.body");

            mailSender.sendEmail(dmiNotificationEmail, fromSystemEmail, applyTemplate(header, environment),
                    applyTemplate(body, environment));

            embryoLogService.info(applyTemplate(header, environment) + " sent to " + requestAccessToEmail);
        } catch (Throwable t) {
            embryoLogService.error("Error sending notificationmail to " + dmiNotificationEmail, t);
            throw new RuntimeException(t);
        }
    }

    @Override
    public void newGreenposReport(GreenPosReport report) {

        try {
            DateTimeConverter reportTsConverter = DateTimeConverter.getDateTimeConverter("MM");

            Map<String, String> environment = new HashMap<>();
            environment.put("VesselName", report.getVesselName());
            environment.put("VesselMmsi", "" + report.getVesselMmsi());
            environment.put("VesselCallSign", report.getVesselCallSign());
            environment.put("ReportTS", reportTsConverter.toString(report.getTs()));
            environment.put("Latitude", report.getPosition().getLatitudeAsString());
            environment.put("Longitude", report.getPosition().getLongitudeAsString());

            String templateName = "greenposReport";

            if (report instanceof GreenPosDMIReport) {
                environment.put("IceInformation", ((GreenPosDMIReport) report).getIceInformation());
                environment.put("Weather", ((GreenPosDMIReport) report).getWeather());
            }

            if (report instanceof GreenPosPositionReport) {
                templateName = "greenposPositionReport";
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
                templateName = "greenposSailingPlanReport";
            }
            if (report instanceof GreenPosFinalReport) {
                templateName = "greenposFinalReport";
            }
            if (report instanceof GreenPosDeviationReport) {
                GreenPosDeviationReport dReport = (GreenPosDeviationReport) report;
                ReportedRoute route = dReport.getRoute();
                environment.put("Deviation", dReport.getDeviation());
                environment.put("RouteWayPoints", route == null ? null : route.getWayPointsAsString());
                templateName = "greenposDeviationReport";

            }

            String header = propertyFileService.getProperty("embryo.notification.template." + templateName + ".header");
            String body = propertyFileService.getProperty("embryo.notification.template." + templateName + ".body");
            String email = subject.getUser().getEmail();

            mailSender.sendEmail(greenposToEmail, email, applyTemplate(header, environment),
                    applyTemplate(body, environment));

            embryoLogService.info(applyTemplate(header, environment) + " sent to " + greenposToEmail);

        } catch (Throwable t) {
            embryoLogService.error("Error sending " + (report != null ? report.getClass().getSimpleName() : null)
                    + "  to " + greenposToEmail, t);
            throw new RuntimeException(t);
        }
    }
}
