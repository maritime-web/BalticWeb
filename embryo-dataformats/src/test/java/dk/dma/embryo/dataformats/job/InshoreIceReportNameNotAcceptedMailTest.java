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
        body += "Inshore ice reports must follow the naming scheme yyyy-MM-dd.txt. The format is described below.\n\n";
        body += "yyyy - The year\n";
        body += "MM - The month of year. Accepted values are 01 - 12\n";
        body += "dd - The day of month. Accepted values are 01 - 31\n";
        body += "An example of a valid value is 2014-07-14.txt\n";

        Assert.assertEquals("arktiskcom@gmail.com", mail.getTo());
        Assert.assertEquals("noreply@dma.dk", mail.getFrom());
        Assert.assertEquals(header, mail.getHeader());
        Assert.assertEquals(body, mail.getBody());
    }

}
