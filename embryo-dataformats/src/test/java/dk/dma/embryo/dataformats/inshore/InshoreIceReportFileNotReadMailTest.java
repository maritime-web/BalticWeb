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
 * Created by Jesper Tejlgaard on 10/22/14.
 */
@RunWith(CdiRunner.class)
@AdditionalClasses(value = {PropertyFileService.class})
public class InshoreIceReportFileNotReadMailTest {


    @Inject
    PropertyFileService propertyFileService;

    @Test
    public void test() {
        // TEST DATA
        String inshoreIceReport = "my inshore ice report";

        Exception cause = new Exception("fileparseproblem");

        // EXECUTE
        InshoreIceReportFileNotReadMail mail = new InshoreIceReportFileNotReadMail("dmi", inshoreIceReport, cause, propertyFileService).build();

        // VERIFY
        String header = "ArcticWeb was not able to parse and import inshore ice report with name " + inshoreIceReport;
        String body = "ArcticWeb was not able to parse import the inshore ice report " + inshoreIceReport + " due to unexpected error during file read: " + cause.getMessage() + "\n";
        body += "Please make sure the file format is as required by ArcticWeb.\n";

        Assert.assertEquals("arktiskcom@gmail.com", mail.getTo());
        Assert.assertEquals("noreply@dma.dk", mail.getFrom());
        Assert.assertEquals(header, mail.getHeader());
        Assert.assertEquals(body, mail.getBody());
    }
}
