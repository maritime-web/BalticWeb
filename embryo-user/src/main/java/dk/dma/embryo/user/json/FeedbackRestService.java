package dk.dma.embryo.user.json;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.common.mail.MailSender;
import dk.dma.embryo.user.mail.FeedbackMail;
import dk.dma.embryo.user.model.Feedback;

@Path("/feedback")
public class FeedbackRestService {
    
    @Inject
    private MailSender mailSender;
    
    @Inject
    private PropertyFileService propertyFileService;

    @POST
    @Path("/")
    @Consumes("application/json")
    public void leaveFeedback(Feedback feedback) {
        mailSender.sendEmail(new FeedbackMail(feedback, propertyFileService));
    }
}
