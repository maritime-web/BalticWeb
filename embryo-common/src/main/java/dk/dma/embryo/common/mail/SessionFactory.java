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

import java.io.Serializable;
import java.util.Properties;

import javax.enterprise.inject.Produces;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import dk.dma.embryo.common.configuration.PropertyFileService;

/**
 * @author Jesper Tejlgaard
 */
public class SessionFactory implements Serializable{

    private static final long serialVersionUID = -8167787224501433444L;

    @Produces
    public Session getMailSession(PropertyFileService propertyFileService) {
        final String enabled = propertyFileService.getProperty("embryo.notification.mail.enabled");
        final String smtpHost = propertyFileService.getProperty("embryo.notification.mail.smtp.host");
        final String username = propertyFileService.getProperty("embryo.notification.mail.smtp.username", "");
        final String password = propertyFileService.getProperty("embryo.notification.mail.smtp.password", "");
        
        if (enabled == null || !"TRUE".equals(enabled.toUpperCase())) {
            return null;
        }

        Properties properties = new Properties();
        properties.put("mail.smtp.host", smtpHost);
        Session session;
        if (username == null || username.trim().equals("")) {
            session = Session.getDefaultInstance(properties);
        } else {
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.port", "587");
            session = Session.getInstance(properties, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
        }
        return session;
    }
}
