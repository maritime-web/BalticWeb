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
