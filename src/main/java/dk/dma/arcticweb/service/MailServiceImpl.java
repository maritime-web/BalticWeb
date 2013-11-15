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
package dk.dma.arcticweb.service;

import dk.dma.configuration.Property;
import dk.dma.configuration.PropertyFileService;
import dk.dma.embryo.domain.GreenPosDMIReport;
import dk.dma.embryo.domain.GreenPosReport;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;

@Named
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class MailServiceImpl implements MailService {

    @Inject
    private Logger logger;

    @Inject
    @Property("embryo.metoc.minDistance")
    private Integer minimumMetocDistance;

    @Inject
    @Property("embryo.notification.mail.to.greenpos")
    private String toEmail;

    @Inject
    @Property("embryo.notification.mail.from")
    private String fromEmail;

    @Inject
    @Property("embryo.notification.mail.smtp.host")
    private String smtpHost;

    @Inject
    @Property(value = "embryo.notification.mail.smtp.username", defaultValue = " ")
    private String username;

    @Inject
    @Property(value = "embryo.notification.mail.smtp.password", defaultValue = " ")
    private String password;

    @Inject
    @Property("embryo.notification.mail.enabled")
    private String enabled;

    @Inject
    private PropertyFileService propertyFileService;

    public MailServiceImpl() {
    }

    @PostConstruct
    public void init() {
        if (enabled == null || !"TRUE".equals(enabled.toUpperCase())) {
            logger.info("ArcticWeb MAIL SERVICE DISABLED");
        } else {
            logger.info("ArcticWeb MAIL SERVICE ENABLED");
        }
    }

    private void sendEmail(String header, String body) {
        if (enabled == null || !"TRUE".equals(enabled.toUpperCase())) {
            logger.info("Email to Arctic Command has been disabled. Would have sent the following:\n" + header + "\n" + body);
            return;
        }

        Properties properties = new Properties();

        properties.put("mail.smtp.host", smtpHost);

        Session session;

        if (username == null || username.trim().equals("-")) {
            session = Session.getDefaultInstance(properties);
        } else {
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.port", "587");

            session = Session.getInstance(properties,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });
        }

        try {
            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress(fromEmail));

            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));


            message.setSubject(header);

            message.setText(body);

            Transport.send(message);

            logger.info("The following email to Arctic Command have been sent:\n" + header + "\n" + body);
        } catch (Exception mex) {
            throw new RuntimeException(mex);
        }
    }

    private String applyTemplate(String template, Map<String, String> environment) {
        String result = template;

        for (String key : environment.keySet()) {
            String value = environment.get(key);

            if (value == null) value = "null";

            value = Matcher.quoteReplacement(value);

            result = result.replaceAll("\\{" + key + "\\}", value);
        }

        return result;
    }

    @Override
    public void newGreenposReport(GreenPosReport report) {
        Map<String, String> environment = new HashMap<>();

        environment.put("VesselName", report.getVesselName());
        environment.put("VesselMmsi", "" + report.getVesselMmsi());
        environment.put("VesselCallSign", report.getVesselCallSign());
        environment.put("Latitude", report.getPosition().getLatitudeAsString());
        environment.put("Longitude", report.getPosition().getLongitudeAsString());

        String templateName = "greenposReport";

        if (report instanceof GreenPosDMIReport) {
            environment.put("IceInformation", ((GreenPosDMIReport) report).getIceInformation());
            environment.put("Weather", ((GreenPosDMIReport) report).getWeather());
        }

        /*
        if (report instanceof GreenPosFinalReport) {
            templateName = "greenposFinalReport";
        } else if (report instanceof GreenPosDeviationReport) {
            templateName = "greenposDeviationReport";
        } else if (report instanceof GreenPosSailingPlanReport) {
            environment.put("Destination", ((GreenPosSailingPlanReport) report).getDestination());
            environment.put("PersonsOnBoard", "" + ((GreenPosSailingPlanReport) report).getPersonsOnBoard());
            environment.put("EtaOfArrival", "" + ((GreenPosSailingPlanReport) report).getEtaOfArrival());
            templateName = "greenposSailingPlanReport";
        } else if (report instanceof GreenPosPositionReport) {
            templateName = "greenposPositionReport";
        }
        */

        String header = propertyFileService.getProperty("embryo.notification.template." + templateName + ".header");
        String body = propertyFileService.getProperty("embryo.notification.template." + templateName + ".body");

        sendEmail(applyTemplate(header, environment), applyTemplate(body, environment));
    }
}
