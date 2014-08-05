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
                logger.info("Email sending has been disabled. Would have sent the following to {} (cc {}):\n{}\n{}",
                        mail.getTo(), mail.getCc(), mail.getHeader(), mail.getBody());
                return;
            }

            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress(mail.getFrom()));

            for (String email : mail.getTo().split(";")) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            }

            if (mail.getCc() != null && mail.getCc().trim().length() > 0) {
                for (String email : mail.getCc().split(";")) {
                    message.addRecipient(Message.RecipientType.CC, new InternetAddress(email));
                }
            }

            message.setSubject(mail.getHeader());
            message.setText(mail.getBody());
            Transport.send(message);

            logger.info("The following email to {} (cc {}) have been sent:\n{}\n{}", mail.getTo(), mail.getCc(),
                    mail.getHeader(), mail.getBody());
            embryoLogService.info(mail.getHeader() + " sent to " + mail.getTo() + " (cc: " + mail.getCc() + ")");
        } catch (Exception mex) {
            embryoLogService.error(
                    "Error sending mail '" + mail.getHeader() + "' to " + mail.getTo() + " (cc: " + mail.getCc() + ")",
                    mex);
            throw new RuntimeException(mex);
        }
    }
}
