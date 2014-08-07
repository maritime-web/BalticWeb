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
package dk.dma.embryo.dataformats.inshore;

import javax.inject.Inject;

import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import dk.dma.embryo.common.configuration.PropertyFileService;

/**
 * @author Jesper Tejlgaard
 */
@RunWith(CdiRunner.class)
@AdditionalClasses(value = { PropertyFileService.class})
public class InshoreIceReportNameNotAcceptedMailTest {

    @Inject
    PropertyFileService propertyFileService;
    
    @Test
    public void test(){
        // TEST DATA
        String inshoreIceReport = "my inshore ice report";

        // EXECUTE
        InshoreIceReportNameNotAcceptedMail mail = new InshoreIceReportNameNotAcceptedMail("dmi", inshoreIceReport, propertyFileService).build();

        // VERIFY
        String header = "ArcticWeb was not able to import inshore ice report with inconsistent name " + inshoreIceReport;
        String body = "ArcticWeb was not able to import the inshore ice report " + inshoreIceReport + ", because it does not follow the expected naming scheme.\n";
        body += "Please delete the inshore ice report " + inshoreIceReport + " from the FTP location ftp.test.dk/mydir.\n";
        body += "Inshore ice report files must follow the naming scheme yyyy-MM-dd[_version].txt, where '_version' is optional. The format is described below.\n\n";
        body += "yyyy - The year\n";
        body += "MM - The month of year. Accepted values are 01 - 12\n";
        body += "dd - The day of month. Accepted values are 01 - 31\n";
        body += "version - The version of the inshore ice report. This attribute is optional. Valid values starts with a v and ends with a positive number, e.g. v1, v2, v3, v10, v15.\n\n";
        body += "Examples of valid values are 2014-07-14.txt and 2014-07-14_v2.txt\n";

        Assert.assertEquals("arktiskcom@gmail.com", mail.getTo());
        Assert.assertEquals("noreply@dma.dk", mail.getFrom());
        Assert.assertEquals(header, mail.getHeader());
        Assert.assertEquals(body, mail.getBody());
    }

}
