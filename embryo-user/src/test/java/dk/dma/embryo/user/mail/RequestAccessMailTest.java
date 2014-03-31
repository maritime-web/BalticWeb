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

import javax.inject.Inject;
import javax.mail.MessagingException;

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
    public void test() throws MessagingException {
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
        body += "Email Address: john@doe.com\n";
        body += "Mmsi Number: 12";

        Assert.assertEquals("arktiskcom@gmail.com", mail.getTo());
        Assert.assertEquals("noreply@dma.dk", mail.getFrom());
        Assert.assertEquals(header, mail.getHeader());
        Assert.assertEquals(body, mail.getBody());
    }

}
