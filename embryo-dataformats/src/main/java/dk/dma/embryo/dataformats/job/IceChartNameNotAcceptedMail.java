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
