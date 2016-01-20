/*
 *  Copyright (c) 2011 Danish Maritime Authority.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package dk.dma.embryo.dataformats.persistence;

import dk.dma.embryo.dataformats.model.ForecastType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by Steen on 20-01-2016.
 *
 */
@RunWith(MockitoJUnitRunner.class)

public class ForecastDataRepositoryTest {
    @Mock
    private HttpCouchClient httpCouchClient;

    @InjectMocks
    private ForecastDataRepository cut;

    private String viewResult = "{\n" +
            "  \"total_rows\": 30,\n" +
            "  \"offset\": 18,\n" +
            "  \"rows\": [\n" +
            "    {\n" +
            "      \"id\": \"Greenland-NEFCOOICE_FORECAST\",\n" +
            "      \"key\": \"FCOO\",\n" +
            "      \"value\": {\n" +
            "        \"area\": \"Greenland NE\",\n" +
            "        \"ftype\": \"ICE_FORECAST\",\n" +
            "        \"size\": 28337,\n" +
            "        \"provider\": \"FCOO\",\n" +
            "        \"name\": \"arcticweb_fcoo_ww3_Greenland_9nm_short_v001C_2016011718.nc\",\n" +
            "        \"timestamp\": 1453053600000\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"Greenland-NEFCOOWAVE_FORECAST\",\n" +
            "      \"key\": \"FCOO\",\n" +
            "      \"value\": {\n" +
            "        \"area\": \"Greenland NE\",\n" +
            "        \"ftype\": \"WAVE_FORECAST\",\n" +
            "        \"size\": 222699,\n" +
            "        \"provider\": \"FCOO\",\n" +
            "        \"name\": \"arcticweb_fcoo_ww3_Greenland_9nm_short_v001C_2016011718.nc\",\n" +
            "        \"timestamp\": 1453053600000\n" +
            "      }\n" +
            "    }]}";

    @Test
    public void shouldExtractValueAndIdFromRawViewResult() throws Exception {
        when(httpCouchClient.getByView(anyString())).thenReturn(viewResult);

        String result = cut.list(ForecastType.Type.CURRENT_FORECAST);

        assertThat(result, not(containsString("value")));
        assertThat(result, containsString("\"id\":\"Greenland-NEFCOOICE_FORECAST\""));
        assertThat(result, containsString("\"ftype\":\"WAVE_FORECAST\""));
    }
}