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

import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;

import dk.dma.configuration.Property;
import dk.dma.embryo.domain.GreenPosReport;

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
    @Property("embryo.notification.mail.enabled")
    private String enabled;

    public MailServiceImpl() {
    }
    
    @PostConstruct
    public void init(){
        if(enabled == null || !"TRUE".equals(enabled.toUpperCase())){
            logger.info("ArcticWeb MAIL SERVICE DISABLED");
        }else{
            logger.info("ArcticWeb MAIL SERVICE ENABLED");
        }
    }

    @Override
    public String newGreenposReport(GreenPosReport report) {
        // Assuming you are sending email from localhost

        if(enabled == null || !"TRUE".equals(enabled.toUpperCase())){
            return "";
        }
        
        final String username = "username@gmail.com";
        final String password = "password";
        
        // Get system properties
        Properties properties = new Properties();

        // Setup mail server
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.port", "587");
        
        // Get the default Session object.
        // Session session = Session.getDefaultInstance(properties);

        Session session = Session.getInstance(properties,
                new javax.mail.Authenticator() {
                  protected PasswordAuthentication getPasswordAuthentication() {
                      return new PasswordAuthentication(username, password);
                  }
                });
        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(fromEmail));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));

            // Set Subject: header field
            message.setSubject("New Greenpos report from " + report.getVesselName());

            // Now set the actual message
            message.setText(report.getVesselName() + " with mmsi " + report.getVesselMmsi() + " and call sign "
                    + report.getVesselCallSign()
                    + " has submitted a new greenpos report. See the details in ArcticWeb.");

            // Send message
            Transport.send(message);
            logger.info("Send email successfully");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }

        // TODO Auto-generated method stub
        return null;
    }

}
