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
package dk.dma.embryo.weather.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;


import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import dk.dma.embryo.common.configuration.LogConfiguration;
import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.common.log.EmbryoLogService;

/**
 * @author Jesper Tejlgaard
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({PropertyFileService.class, LogConfiguration.class})

public class DmiWeatherJobIT {
    
    @Inject
    DmiWeatherJob job;
    
    @Produces
    @Mock
    EmbryoLogService logservice;
    
    @Inject
    @Property(value = "embryo.weather.dmi.localDirectory", substituteSystemProperties = true)
    String localDmiDir;
    
    @Test
    public void testJob() {
        job.timeout();

        Mockito.verify(logservice).info("Scanned DMI (ftpserver.dmi.dk) for files. Transfered: gronvar.xml, gruds.xml, Errors: ");
        
        Path localDir = Paths.get(localDmiDir);
        
        Assert.assertTrue(Files.exists(localDir.resolve("gronvar.xml")));
        Assert.assertTrue(Files.exists(localDir.resolve("gruds.xml")));
    }

}
