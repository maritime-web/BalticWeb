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
