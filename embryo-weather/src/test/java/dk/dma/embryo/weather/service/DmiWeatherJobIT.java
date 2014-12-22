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
package dk.dma.embryo.weather.service;

import dk.dma.embryo.common.configuration.LogConfiguration;
import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.common.log.EmbryoLogService;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

        Mockito.verify(logservice).info("Scanned DMI (ftpserver.dmi.dk) for files. Transfered: gronvar-2014-12-09.xml, gruds.xml, Errors: ");
        
        Path localDir = Paths.get(localDmiDir);

        Assert.assertTrue(Files.exists(localDir.resolve("dmi/gronvar-2014-12-09.xml")));
        Assert.assertTrue(Files.exists(localDir.resolve("gruds.xml")));
    }

}
