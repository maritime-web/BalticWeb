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
package dk.dma.arcticweb.filetransfer;

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
        IceChartNotificationMail mail = new IceChartNotificationMail("dmi", iceChart, t, propertyFileService).build();

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
