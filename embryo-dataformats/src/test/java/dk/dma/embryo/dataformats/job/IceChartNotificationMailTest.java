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

import javax.ejb.EJBException;
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
public class IceChartNotificationMailTest {

    @Inject
    PropertyFileService propertyFileService;
    
    @Test
    public void test(){

        // TEST DATA
        String iceChart = "my ice chart";
        Throwable t = new EJBException(new RuntimeException("Expected to read lots of bytes"));

        // EXECUTE
        ShapeNotificationMail mail = new ShapeNotificationMail("dmi", iceChart, t, propertyFileService).build();

        // VERIFY
        String header = "ArcticWeb detected an error importing ice chart " + iceChart;
        String body = "Ice Chart: " + iceChart + "\n";
        body += "Message: Possible corrupt ice chart. You may want to delete the ice chart.\n";
        body += "Error: java.lang.RuntimeException: Expected to read lots of bytes";

        Assert.assertEquals("arktiskcom@gmail.com", mail.getTo());
        Assert.assertEquals("noreply@dma.dk", mail.getFrom());
        Assert.assertEquals(header, mail.getHeader());
        Assert.assertEquals(body, mail.getBody());
    }

}
