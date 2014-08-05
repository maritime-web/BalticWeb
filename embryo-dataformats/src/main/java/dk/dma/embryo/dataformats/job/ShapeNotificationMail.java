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

import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.common.mail.Mail;

/**
 * @author Jesper Tejlgaard
 */
public class ShapeNotificationMail extends Mail<ShapeNotificationMail>{

    private final Throwable error;
    private final String iceChart;
    private final String provider;
    
    public ShapeNotificationMail(String provider, String iceChart, Throwable t, PropertyFileService propertyFileService) {
        super("icechartImportError", propertyFileService);
        this.provider = provider;
        this.error = t;
        this.iceChart = iceChart;
    }
    
    public ShapeNotificationMail build(){
        String msg = error.getMessage();
        if (msg.contains("Expected to read")) {
            msg = "Possible corrupt ice chart. You may want to delete the ice chart.";
        }
        environment.put("IceChart", iceChart);
        environment.put("Message", msg);
        environment.put("Error", error.getMessage());

        setTo(propertyFileService.getProperty("embryo.iceChart." + provider + ".notification.email"));
        setFrom(propertyFileService.getProperty("embryo.notification.mail.from"));
        
        return this;
    }

}
