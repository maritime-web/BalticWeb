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
import dk.dma.embryo.user.json.ForgotPasswordRestService.ForgotPasswordRequest;

/**
 * @author Jacob Avlund
 */
public class ForgotPasswordMail extends Mail<ForgotPasswordMail> {

    private final ForgotPasswordRequest request;
    
    public ForgotPasswordMail(ForgotPasswordRequest request, PropertyFileService propertyFileService) {
        super("forgotPassword", propertyFileService);
        this.request = request;
    }

    public ForgotPasswordMail build() {
        environment.put("UserName", request.getUsername());
        environment.put("Link", request.getHost() + "/content.html#/changePassword/" + request.getUuid());

        setTo(request.getEmailAddress());
        setFrom(propertyFileService.getProperty("embryo.notification.mail.from"));

        return this;
    }
}
