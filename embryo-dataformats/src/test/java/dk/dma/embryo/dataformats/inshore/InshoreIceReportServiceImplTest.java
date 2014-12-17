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

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class InshoreIceReportServiceImplTest {


    @Test
    public void testUpdateError() throws IOException, InshoreIceReportException {
        String dir = getClass().getResource("/inshore-ice-reports/testUpdateError").getFile();

        InshoreIceReportServiceImpl service = new InshoreIceReportServiceImpl(dir, 10000000);

        try {
            service.update();

            Assert.assertTrue("Exception should have been thrown", false);
        } catch (InshoreIceReportException iire) {
            Assert.assertTrue(iire.getCauses().containsKey("2014-10-20.txt"));
            Assert.assertTrue(iire.getCauses().get("2014-10-20.txt") instanceof Exception);
        }

    }

}
