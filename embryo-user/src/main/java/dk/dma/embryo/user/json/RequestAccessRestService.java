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
package dk.dma.embryo.user.json;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.common.mail.MailSender;
import dk.dma.embryo.user.mail.RequestAccessMail;

@Path("/request-access")
public class RequestAccessRestService {
    @Inject
    MailSender mailSender;
    
    @Inject
    PropertyFileService propertyFileService;
    
    @POST
    @Path("/save")
    @Consumes("application/json")
    public void save(SignupRequest request) {
        mailSender.sendEmail(new RequestAccessMail(request, propertyFileService));
    }

    public static class SignupRequest {
        private String preferredLogin;
        private String contactPerson;
        private String emailAddress;
        private Long mmsiNumber;

        public String getPreferredLogin() {
            return preferredLogin;
        }

        public void setPreferredLogin(String preferredLogin) {
            this.preferredLogin = preferredLogin;
        }

        public String getContactPerson() {
            return contactPerson;
        }

        public void setContactPerson(String contactPerson) {
            this.contactPerson = contactPerson;
        }

        public String getEmailAddress() {
            return emailAddress;
        }

        public void setEmailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
        }

        public Long getMmsiNumber() {
            return mmsiNumber;
        }

        public void setMmsiNumber(Long mmsiNumber) {
            this.mmsiNumber = mmsiNumber;
        }
    }
}
