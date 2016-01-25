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

package dk.dma.embryo.dataformats.model;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

/**
 * Created by Steen on 18-01-2016.
 *
 */
public class ForecastDataTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void shouldMergeMetaDataIntoTheJson() throws Exception {
        String json = "{\"metadata\": {\"lon\":\"some longitude\"}}";
        ForecastDataId id = new ForecastDataId("NW Greenland", ForecastProvider.DMI, Type.WAVE_FORECAST);
        ForecastData cut = new ForecastData(id, json);

        assertThat(cut.getJson(), allOf(containsString("NW Greenland"), containsString(ForecastProvider.DMI.name()), containsString(Type.WAVE_FORECAST.name())) );
    }
}
