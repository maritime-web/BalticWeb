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

import javax.inject.Inject;

import org.jglue.cdiunit.CdiRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.user.json.RequestAccessRestService.SignupRequest;

/**
 * @author Jesper Tejlgaard
 */
@RunWith(CdiRunner.class)
public class RequestAccessMailTest {

    @Inject
    private PropertyFileService propertyFileService;
    
    @Test
    public void test() throws Exception {
        // TEST DATA
        SignupRequest request = new SignupRequest();
        request.setContactPerson("John Doe");
        request.setEmailAddress("john@doe.com");
        request.setMmsiNumber(12L);
        request.setPreferredLogin("john");

        // EXECUTE
        RequestAccessMail mail = new RequestAccessMail(request, propertyFileService).build();

        // VERIFY
        String header = "Request Access for john@doe.com";
        String body = "Preferred Login: john\n";
        body += "Contact Person: John Doe\n";
        body += "E-mail Address: john@doe.com\n";
        body += "Mmsi Number: 12";

        Assert.assertEquals("arktiskcom@gmail.com", mail.getTo());
        Assert.assertEquals("noreply@dma.dk", mail.getFrom());
        Assert.assertEquals(header, mail.getHeader());
        Assert.assertEquals(body, mail.getBody());
    }

}
