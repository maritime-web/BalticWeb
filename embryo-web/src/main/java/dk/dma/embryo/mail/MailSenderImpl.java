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
package dk.dma.embryo.mail;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;

import dk.dma.embryo.common.configuration.Property;

/**
 * @author Jesper Tejlgaard
 */
@Named
public class MailSenderImpl implements MailSender{
    
    @Inject
    @Property("embryo.notification.mail.enabled")
    private String enabled;

    @Inject
    private Session session;

    @Inject
    private Logger logger;

    @PostConstruct
    public void init() {
        if (enabled == null || !"TRUE".equals(enabled.toUpperCase())) {
            logger.info("ArcticWeb MAIL SERVICE DISABLED");
        } else {
            logger.info("ArcticWeb MAIL SERVICE ENABLED");
        }
    }

    public void sendEmail(String toEmail, String from, String header, String body) {
        logger.debug("enabled=" + enabled);

        if (enabled == null || !"TRUE".equals(enabled.toUpperCase())) {
            logger.info("Email sending has been disabled. Would have sent the following to " + toEmail + ":\n" + header
                    + "\n" + body);
            return;
        }

        try {
            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress(from));

            for (String email : toEmail.split(";")) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            }

            message.setSubject(header);

            message.setText(body);

            Transport.send(message);

            logger.info("The following email to " + toEmail + " have been sent:\n" + header + "\n" + body);
        } catch (Exception mex) {
            throw new RuntimeException(mex);
        }
    }

}
