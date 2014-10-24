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
import dk.dma.embryo.user.model.Feedback;

public class FeedbackMail extends Mail<FeedbackMail> {

    private Feedback feedback;
    
    public FeedbackMail(Feedback feedback, PropertyFileService propertyFileService) {
        super("feedback", propertyFileService);
        this.feedback = feedback;
    }
    
    
    @Override
    public FeedbackMail build() {
        environment.put("Name", feedback.getName());
        environment.put("Email", feedback.getEmailAddress());
        environment.put("UserType", feedback.getUserType());
        environment.put("MMSI", feedback.getMmsiNumber());
        environment.put("Message", feedback.getMessage());
        
        setTo(propertyFileService.getProperty("embryo.notification.mail.to.feedback"));
        setFrom(feedback.getEmailAddress());
        
        return this;
    }

}
