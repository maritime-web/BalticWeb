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

import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.common.mail.Mail;

/**
 * @author Jesper Tejlgaard
 */
public class InshoreIceReportNameNotAcceptedMail extends Mail<InshoreIceReportNameNotAcceptedMail> {

    private final String inshoreIceReport;
    private final String provider;

    public InshoreIceReportNameNotAcceptedMail(String provider, String inshoreIceReport, 
            PropertyFileService propertyFileService) {
        super("inshoreIceReportNameNotAccepted", propertyFileService);
        this.provider = provider;
        this.inshoreIceReport = inshoreIceReport;
    }

    public InshoreIceReportNameNotAcceptedMail build() {

        String ftpLocation = propertyFileService.getProperty("embryo.inshoreIceReport.dmi.ftp.serverName")
                + propertyFileService.getProperty("embryo.inshoreIceReport.dmi.ftp.baseDirectory");

        environment.put("InshoreIceReport", inshoreIceReport);
        environment.put("FTPLocation", ftpLocation);

        setTo(propertyFileService.getProperty("embryo.inshoreIceReport." + provider + ".notification.email"));
        setFrom(propertyFileService.getProperty("embryo.notification.mail.from"));
        return this;
    }

}
