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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Steen on 12-01-2016.
 */
public class ForecastDataIdTest {
    @Test
    public void shouldCreateIdStringByConcatenatingConstructorInputParameters() throws Exception {
        String area = "NE";
        ForecastProvider provider = ForecastProvider.DMI;
        Type type = Type.CURRENT_FORECAST;
        ForecastDataId cut = new ForecastDataId(area, provider, type);

        assertThat(cut.getId(), is(area + provider + type));
    }

    @Test(expected = NullPointerException.class)
    public void shouldRequireNonNullValueForArea() throws Exception {
        String area = null;
        ForecastProvider provider = ForecastProvider.DMI;
        Type type = Type.CURRENT_FORECAST;

        new ForecastDataId(area, provider, type);
    }

    @Test(expected = NullPointerException.class)
    public void shouldRequireNonNullValueForProvider() throws Exception {
        String area = "NE";
        ForecastProvider provider = null;
        Type type = Type.CURRENT_FORECAST;

        new ForecastDataId(area, provider, type);
    }

    @Test(expected = NullPointerException.class)
    public void shouldRequireNonNullValueForType() throws Exception {
        String area = "NE";
        ForecastProvider provider = ForecastProvider.DMI;
        Type type = null;

        new ForecastDataId(area, provider, type);
    }

}
