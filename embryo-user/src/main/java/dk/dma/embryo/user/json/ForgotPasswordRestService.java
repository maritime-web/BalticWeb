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
package dk.dma.embryo.user.json;

import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.common.mail.MailSender;
import dk.dma.embryo.user.mail.ForgotPasswordMail;
import dk.dma.embryo.user.model.SecuredUser;
import dk.dma.embryo.user.security.Subject;
import dk.dma.embryo.user.service.UserService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

@Path("/forgot-password")
public class ForgotPasswordRestService {
    @Inject
    private MailSender mailSender;

    @Inject
    private Subject subject;

    @Inject
    private PropertyFileService propertyFileService;

    @Inject
    private UserService userService;

    @POST
    @Path("/request")
    @Consumes("application/json")
    @Produces("application/json")
    public void save(ForgotPasswordRequest request, @Context UriInfo uriInfo) {
        try {
            URI baseUri = uriInfo.getBaseUri();
            SecuredUser user = subject.getUserForEmail(request.getEmailAddress());
            if (user != null) {
                userService.createPasswordUuid(user);
                request.setUsername(user.getUserName());
                request.setUuid(user.getForgotUuid());
                request.setHost(baseUri.getHost());
                mailSender.sendEmail(new ForgotPasswordMail(request, propertyFileService));
                return;
            }
        } catch (RuntimeException e) {
            throw new WebApplicationException(Response.serverError().type(MediaType.APPLICATION_JSON).entity(e.getMessage()).build());
        }
        throw new NoUserWithEmail("No user found for e-mail address " + request.getEmailAddress());
    }

    public static class ForgotPasswordRequest {
        private String username;
        private String emailAddress;
        private String uuid;
        private String host;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmailAddress() {
            return emailAddress;
        }

        public void setEmailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }
    }

    public static class NoUserWithEmail extends WebApplicationException {
        private static final long serialVersionUID = 2137339598766843707L;

        public NoUserWithEmail(String message) {
            super(Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON).entity(message).build());
        }
    }
}
