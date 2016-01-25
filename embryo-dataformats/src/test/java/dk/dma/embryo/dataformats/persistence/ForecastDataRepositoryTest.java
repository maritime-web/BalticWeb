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

package dk.dma.embryo.dataformats.persistence;

import dk.dma.embryo.dataformats.model.ForecastDataId;
import dk.dma.embryo.dataformats.model.ForecastHeader;
import dk.dma.embryo.dataformats.model.ForecastProvider;
import dk.dma.embryo.dataformats.model.Type;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by Steen on 20-01-2016.
 *
 */
@SuppressWarnings("FieldCanBeLocal")
@RunWith(MockitoJUnitRunner.class)

public class ForecastDataRepositoryTest {
    @Mock
    private HttpCouchClient httpCouchClient;

    @InjectMocks
    private ForecastDataRepository cut;

    private String viewResultMulti = "{\n" +
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
    private String viewResultSingle = "{\n" +
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
            "    }\n]}";

    @Test
    public void shouldContainOneForecastHeaderForEachJsonObject() throws Exception {
        when(httpCouchClient.getByView(anyString())).thenReturn(viewResultMulti);

        List<ForecastHeader> result = cut.list(Type.CURRENT_FORECAST);

        assertThat(result, iterableWithSize(2));
    }

    @Test
    public void shouldReturnForecastHeaderWithIdFieldsWhenNoDataFound() throws Exception {
        when(httpCouchClient.getByView(anyString())).thenReturn("{\n" +
                "  \"total_rows\": 30,\n" +
                "  \"offset\": 0,\n" +
                "  \"rows\": []\n" +
                "}");

        ForecastDataId id = new ForecastDataId("a", ForecastProvider.DMI, Type.CURRENT_FORECAST);
        ForecastHeader result = cut.getForecastHeader(id);

        assertThat(result.getId(), is(equalTo(id.getId())));
    }

    @Test
    public void shouldExtractValueAndIdFromRawViewResult() throws Exception {
        when(httpCouchClient.getByView(anyString())).thenReturn(viewResultSingle);

        ForecastHeader result = cut.list(Type.CURRENT_FORECAST).get(0);

        assertThat(result.getId(), equalTo("Greenland-NEFCOOICE_FORECAST"));
        assertThat(result.getArea(), equalTo("Greenland NE"));
        assertThat(result.getFtype(), equalTo(Type.ICE_FORECAST));
        assertThat(result.getSize(), equalTo(28337));
        assertThat(result.getProvider(), equalTo(ForecastProvider.FCOO));
        assertThat(result.getName(), equalTo("arcticweb_fcoo_ww3_Greenland_9nm_short_v001C_2016011718.nc"));
        assertThat(result.getTimestamp(), equalTo(1453053600000L));
    }
}
