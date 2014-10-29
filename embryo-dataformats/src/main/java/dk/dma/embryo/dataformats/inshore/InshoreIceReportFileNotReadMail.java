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
public class InshoreIceReportFileNotReadMail extends Mail<InshoreIceReportFileNotReadMail> {

    private final String inshoreIceReportFile;
    private final String provider;
    private final Exception cause;

    public InshoreIceReportFileNotReadMail(String provider, String inshoreIceReportFile, Exception cause,
                                           PropertyFileService propertyFileService) {
        super("inshoreIceReportFileNotRead", propertyFileService);
        this.provider = provider;
        this.inshoreIceReportFile = inshoreIceReportFile;
        this.cause = cause;
    }

    public InshoreIceReportFileNotReadMail build() {
        environment.put("InshoreIceReport", inshoreIceReportFile);
        environment.put("Error", cause.getMessage());

        setTo(propertyFileService.getProperty("embryo.inshoreIceReport." + provider + ".notification.email"));
        setFrom(propertyFileService.getProperty("embryo.notification.mail.from"));
        return this;
    }

}
