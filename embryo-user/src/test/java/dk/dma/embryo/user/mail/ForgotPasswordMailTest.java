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
import org.jglue.cdiunit.CdiRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static dk.dma.embryo.user.json.ForgotPasswordRestService.ForgotPasswordRequest;

/**
 * @author Jesper Tejlgaard
 */
@RunWith(CdiRunner.class)
public class ForgotPasswordMailTest {

    @Inject
    private PropertyFileService propertyFileService;

    @Test
    public void test() throws Exception {
        // TEST DATA
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setUsername("John Doe");
        request.setEmailAddress("john@doe.com");
        request.setHost("http://localhost");
        request.setUuid("UUID");

        // EXECUTE
        ForgotPasswordMail mail = new ForgotPasswordMail(request, propertyFileService).build();

        // VERIFY
        String header = "New password requested for John Doe";
        String body = "You receive this e-mail because you have asked for a password reset.\n";
        body += "Please follow this <a href=\"http://localhost/content.html#/changePassword/UUID\">link</a> to reset your password.";

        Assert.assertEquals("john@doe.com", mail.getTo());
        Assert.assertEquals("noreply@dma.dk", mail.getFrom());
        Assert.assertEquals(header, mail.getHeader());
        Assert.assertEquals(body, mail.getBody());
    }

}
