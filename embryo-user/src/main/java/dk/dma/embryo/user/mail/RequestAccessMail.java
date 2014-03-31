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
package dk.dma.embryo.user.mail;

import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.common.mail.Mail;
import dk.dma.embryo.user.json.RequestAccessRestService.SignupRequest;

/**
 * @author Jesper Tejlgaard
 */
public class RequestAccessMail extends Mail<RequestAccessMail> {

    private final SignupRequest request;
    
    public RequestAccessMail(SignupRequest request, PropertyFileService propertyFileService) {
        super("signupRequest", propertyFileService);
        this.request = request;
    }

    public RequestAccessMail build() {
        environment.put("PreferredLogin", request.getPreferredLogin());
        environment.put("ContactPerson", request.getContactPerson());
        environment.put("EmailAddress", request.getEmailAddress());
        environment.put("MmsiNumber", request.getMmsiNumber() != null ? ("" + request.getMmsiNumber()) : "-");

        setTo(propertyFileService.getProperty("embryo.notification.mail.to.requestAccess"));
        setFrom(propertyFileService.getProperty("embryo.notification.mail.from"));

        return this;
    }
}
