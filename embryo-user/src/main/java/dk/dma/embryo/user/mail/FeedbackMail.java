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
        environment.put("MMSI", feedback.getMmsiNumber());
        environment.put("Message", feedback.getMessage());
        
        setTo(propertyFileService.getProperty("embryo.notification.mail.to.feedback"));
        setFrom(feedback.getEmailAddress());
        
        return this;
    }

}
