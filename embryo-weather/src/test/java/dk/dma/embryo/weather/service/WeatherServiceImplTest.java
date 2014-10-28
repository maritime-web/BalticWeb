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

import java.io.IOException;

import javax.inject.Inject;

import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import dk.dma.embryo.common.configuration.LogConfiguration;
import dk.dma.embryo.common.configuration.PropertyFileService;

@RunWith(CdiRunner.class)
@AdditionalClasses({ PropertyFileService.class, LogConfiguration.class })

public class WeatherServiceImplTest {
    
    @Inject
    private WeatherServiceImpl weatherServiceImpl;

    @Ignore
    @Test
    public void testRefresh() throws IOException {
        weatherServiceImpl.refresh();
    }

}
