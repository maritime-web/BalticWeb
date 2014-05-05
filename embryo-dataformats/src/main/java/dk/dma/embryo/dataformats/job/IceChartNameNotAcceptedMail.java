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

import java.util.Set;

import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.common.mail.Mail;

/**
 * @author Jesper Tejlgaard
 */
public class IceChartNameNotAcceptedMail extends Mail<IceChartNameNotAcceptedMail> {

    private final String iceChart;
    private final Set<String> regions;
    private final String provider;

    public IceChartNameNotAcceptedMail(String provider, String iceChart, Set<String> regions,
            PropertyFileService propertyFileService) {
        super("icechartNameNotAccepted", propertyFileService);
        this.provider = provider;
        this.iceChart = iceChart;
        this.regions = regions;
    }

    public IceChartNameNotAcceptedMail build() {

        String ftpLocation = propertyFileService.getProperty("embryo.iceChart.dmi.ftp.serverName")
                + propertyFileService.getProperty("embryo.iceChart.dmi.ftp.baseDirectory");

        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (String region : regions) {
            if (!first) {
                builder.append(", ");
            } else {
                first = false;
            }
            builder.append(region);
        }
        environment.put("IceChart", iceChart);
        environment.put("Regions", builder.toString());
        environment.put("FTPLocation", ftpLocation);

        setTo(propertyFileService.getProperty("embryo.iceChart." + provider + ".notification.email"));
        setFrom(propertyFileService.getProperty("embryo.notification.mail.from"));

        return this;
    }

}
