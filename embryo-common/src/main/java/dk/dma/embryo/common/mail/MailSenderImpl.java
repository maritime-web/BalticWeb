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
package dk.dma.embryo.common.mail;

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
import dk.dma.embryo.common.log.EmbryoLogService;

/**
 * @author Jesper Tejlgaard
 */
@Named
public class MailSenderImpl implements MailSender {

    @Inject
    @Property("embryo.notification.mail.enabled")
    private String enabled;

    @Inject
    private Session session;

    @Inject
    private EmbryoLogService embryoLogService;

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

    public void sendEmail(Mail<?> mail) {
        logger.debug("enabled={}", enabled);
        
        try {
            mail.build();
            
            if (enabled == null || !"TRUE".equals(enabled.toUpperCase())) {
                logger.info("Email sending has been disabled. Would have sent the following to {}:\n{}\n{}", mail.getTo(),
                        mail.getHeader(), mail.getBody());
                return;
            }

            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress(mail.getFrom()));

            for (String email : mail.getTo().split(";")) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            }

            message.setSubject(mail.getHeader());
            message.setText(mail.getBody());
            Transport.send(message);

            logger.info("The following email to {} have been sent:\n{}\n{}", mail.getTo(), mail.getHeader(),
                    mail.getBody());
            embryoLogService.info(mail.getHeader() + " sent to " + mail.getTo());
        } catch (Exception mex) {
            embryoLogService.error("Error sending mail '" + mail.getHeader() + "' to " + mail.getTo(), mex);
            throw new RuntimeException(mex);
        }
    }

}