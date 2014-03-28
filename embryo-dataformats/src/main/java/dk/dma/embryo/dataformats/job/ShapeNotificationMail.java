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
