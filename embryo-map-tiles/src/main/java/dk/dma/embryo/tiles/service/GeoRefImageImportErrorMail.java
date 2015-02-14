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
package dk.dma.embryo.tiles.service;

import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.common.mail.Mail;

/**
 * @author Jesper Tejlgaard
 */
public class GeoRefImageImportErrorMail extends Mail<GeoRefImageImportErrorMail> {

    private final String imageFile;
    private final Exception exception;
    private final String provider;

    public GeoRefImageImportErrorMail(String imageFile, String provider, Exception e, PropertyFileService propertyFileService) {
        super("georefImageImportError", propertyFileService);
        this.provider = provider;
        this.imageFile = imageFile;
        this.exception = e;
    }

    public GeoRefImageImportErrorMail build() {
        environment.put("Image", imageFile);
        //environment.put("Message", builder.toString());
        environment.put("Error", exception.getMessage());

        setTo(propertyFileService.getProperty("embryo.tiles.providers." + provider + ".notification.email"));
        setFrom(propertyFileService.getProperty("embryo.notification.mail.from"));

        return this;
    }

}
