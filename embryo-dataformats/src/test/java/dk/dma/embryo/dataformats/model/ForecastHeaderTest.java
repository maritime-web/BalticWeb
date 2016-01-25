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

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by Steen on 21-01-2016.
 *
 */
public class ForecastHeaderTest {
    @Test
    public void shouldMapValuesFromMetaDataIntoTheHeader() throws Exception {
        ForecastMetaData metaData = new ForecastMetaData(new ForecastDataId("some area", ForecastProvider.DMI, Type.CURRENT_FORECAST));
        ForecastHeader cut = ForecastHeader.createFrom(metaData);

        assertThat(cut.getArea(), equalTo("some area"));
        assertThat(cut.getProvider(), equalTo(ForecastProvider.DMI));
        assertThat(cut.getFtype(), equalTo(Type.CURRENT_FORECAST));
        assertThat(cut.getId(), is(not(nullValue())));
    }

    @Test
    public void shouldMapAllRecognizedValuesFromJsonIntoTheHeader() throws Exception {
        String json = "{\"id\": \"apt\",\"area\":\"somewhere\"}";
        ForecastHeader cut = ForecastHeader.createFrom(json);

        assertThat(cut.toString(), allOf(containsString("somewhere"), containsString("apt")));
    }

    @Test
    public void shouldIgnoreUnrecognizedValuesWhenCreatingFromJson() throws Exception {
        String json = "{\"arrgh\":\"damn\", \"area\":\"somewhere\"}";
        ForecastHeader cut = ForecastHeader.createFrom(json);

        assertThat(cut.toString(), containsString("somewhere"));
    }

    @Test
    public void shouldAcceptEmptyStringAsParameterWhenCreatingFromJson() throws Exception {
        String json = "";
        ForecastHeader cut = ForecastHeader.createFrom(json);

        assertThat(cut, is(not(nullValue(ForecastHeader.class))));
    }

    @Test
    public void shouldNotBeBetterIfRequiredFieldsForIdAreNotPresent() throws Exception {
        ForecastHeader cut = new ForecastHeader();
        ForecastHeader other = new ForecastHeader();

        assertThat(cut.isBetterThan(other), is(false));
    }

    @Test
    public void shouldNotBeBetterIfNewer() throws Exception {
        ForecastHeader cut = getForecastHeaderWithTimestamp(2);
        ForecastHeader other = getForecastHeaderWithTimestamp(1);

        assertThat(cut.isBetterThan(other), is(true));
    }

    @Test
    public void shouldNotBeBetterIfSamefileButBigger() throws Exception {
        ForecastHeader cut = getForecastHeaderWithSize(2);
        ForecastHeader other = getForecastHeaderWithSize(1);

        assertThat(cut.isBetterThan(other), is(true));
    }

    private ForecastHeader getForecastHeaderWithSize(int size) {
        ForecastDataId defaultId = new ForecastDataId("some area", ForecastProvider.DMI, Type.CURRENT_FORECAST);
        return ForecastHeader.createFrom(new ForecastMetaData(defaultId).withTimestamp(1).withOriginalFileName("a file").withJsonSize(size));
    }

    private ForecastHeader getForecastHeaderWithTimestamp(long timestamp) {
        ForecastDataId defaultId = new ForecastDataId("some area", ForecastProvider.DMI, Type.CURRENT_FORECAST);
        return ForecastHeader.createFrom(new ForecastMetaData(defaultId).withTimestamp(timestamp));
    }


}
