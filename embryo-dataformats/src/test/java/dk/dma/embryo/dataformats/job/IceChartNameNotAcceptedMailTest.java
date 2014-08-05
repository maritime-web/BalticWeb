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
package dk.dma.embryo.dataformats.job;

import java.util.SortedSet;
import java.util.TreeSet;

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
public class IceChartNameNotAcceptedMailTest {

    @Inject
    PropertyFileService propertyFileService;
    
    @Test
    public void test(){

        // TEST DATA
        String iceChart = "my ice chart";
        SortedSet<String> regions = new TreeSet<>();
        regions.add("region1_RIC");
        regions.add("region2_WA");

        // EXECUTE
        IceChartNameNotAcceptedMail mail = new IceChartNameNotAcceptedMail("dmi", iceChart, regions, propertyFileService).build();

        // VERIFY
        String header = "ArcticWeb was not able to import ice chart with inconsistent name " + iceChart;
        String body = "ArcticWeb was not able to import the ice chart " + iceChart + ", because it does not follow the expected naming scheme.\n";
        body += "Please delete the ice chart " + iceChart + " from the FTP location ftp.test.dk/mydir.\n";
        body += "Ice charts must follow the naming scheme yyyyMMddHHmm_region[_version] where '_version' is optional. The format is described below.\n\n";
        body += "yyyy - The year\n";
        body += "MM - The month of year. Accepted values are 01 - 12\n";
        body += "dd - The day of month. Accepted values are 01 - 31\n";
        body += "HH - The hour of day. Accepted values are 00 - 23.\n";
        body += "mm - The minute of hour. Accepted values are 00 - 59.\n";
        body += "region - An ice chart region. Accepted values are region1_RIC, region2_WA\n";
        body += "version - The version of the ice chart in question. This attribute is optional. Valid values starts with a v and ends with a positive number, e.g. v1, v2, v3, v10, v15.\n\n";
        body += "Examples of valid values are 201401231045_CapeFarewell_RIC and 201401231045_CapeFarewell_RIC_v2\n";

        Assert.assertEquals("arktiskcom@gmail.com", mail.getTo());
        Assert.assertEquals("noreply@dma.dk", mail.getFrom());
        Assert.assertEquals(header, mail.getHeader());
        Assert.assertEquals(body, mail.getBody());
    }

}
