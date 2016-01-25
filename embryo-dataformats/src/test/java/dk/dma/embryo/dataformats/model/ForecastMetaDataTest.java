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

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Steen on 19-01-2016.
 *
 */
public class ForecastMetaDataTest {

    @Test
    public void shouldExtractTypeMetaDataFromId() throws Exception {
        Type type = Type.CURRENT_FORECAST;
        ForecastMetaData cut = new ForecastMetaData(getForecastIdWithType(type));

        Map<?, ?> headerMap = getHeaderMap(cut.asMap());
        assertThat(headerMap, hasKey(equalTo("ftype")));
        assertThat(headerMap, hasValue(equalTo(type.name())));
    }

    @Test
    public void shouldExtractAreaMetaDataFromId() throws Exception {
        String area = "NW Gr.";
        ForecastMetaData cut = new ForecastMetaData(getForecastIdWithArea(area));

        Map<?, ?> headerMap = getHeaderMap(cut.asMap());
        assertThat(headerMap, hasKey(equalTo("area")));
        assertThat(headerMap, hasValue(equalTo(area)));
    }

    @Test
    public void shouldExtractProviderMetaDataFromId() throws Exception {
        ForecastProvider provider = ForecastProvider.DMI;
        ForecastMetaData cut = new ForecastMetaData(getForecastIdWithProvider(provider));

        Map<?, ?> headerMap = getHeaderMap(cut.asMap());
        assertThat(headerMap, hasKey(equalTo("provider")));
        assertThat(headerMap, hasValue(equalTo(provider.name())));
    }

    @Test
    public void shouldIncludeJsonSize() throws Exception {
        ForecastMetaData cut = new ForecastMetaData();
        int jsonSize = 2;
        cut = cut.withJsonSize(jsonSize);

        Map<?, ?> headerMap = getHeaderMap(cut.asMap());
        assertThat(headerMap, hasKey(equalTo("size")));
        assertThat(headerMap, hasValue(equalTo(jsonSize)));
    }

    @Test
    public void shouldIncludeTimestamp() throws Exception {
        ForecastMetaData cut = new ForecastMetaData();
        long timestamp = 14098039845089L;
        cut = cut.withTimestamp(timestamp);

        Map<?, ?> headerMap = getHeaderMap(cut.asMap());
        assertThat(headerMap, hasKey(equalTo("timestamp")));
        assertThat(headerMap, hasValue(equalTo(timestamp)));
    }

    @Test
    public void shouldIncludeOriginalfileName() throws Exception {
        ForecastMetaData cut = new ForecastMetaData();
        String fileName = "some name";
        cut = cut.withOriginalFileName(fileName);

        Map<?, ?> headerMap = getHeaderMap(cut.asMap());
        assertThat(headerMap, hasKey(equalTo("name")));
        assertThat(headerMap, hasValue(equalTo(fileName)));
    }

    @Test
    public void shouldCreateNewIncludingUnionOfMetadataWhenMergingWithOther() throws Exception {
        int jsonSize = 2;
        String fileName = "some name";
        ForecastMetaData cut = new ForecastMetaData().withOriginalFileName(fileName);
        ForecastMetaData otherCut = new ForecastMetaData().withJsonSize(jsonSize);

        ForecastMetaData mergedCut = cut.with(otherCut);

        Map<?, ?> headerMap = getHeaderMap(mergedCut.asMap());
        assertThat(headerMap, allOf(hasKey(equalTo("name")), hasKey(equalTo("size"))));
        assertThat(headerMap, hasValue(equalTo(jsonSize)));
        assertThat(headerMap, hasValue(equalTo(fileName)));
    }

    @Test
    public void shouldUseValueOfOtherMetaDataIfBothHaveSameEntry() throws Exception {
        String fileName = "some name";
        String otherFileName = "some other name";
        ForecastMetaData cut = new ForecastMetaData().withOriginalFileName(fileName);
        ForecastMetaData otherCut = new ForecastMetaData().withOriginalFileName(otherFileName);

        ForecastMetaData mergedCut = cut.with(otherCut);

        Map<?, ?> headerMap = getHeaderMap(mergedCut.asMap());
        assertThat(headerMap, hasKey(equalTo("name")));
        assertThat(headerMap, hasValue(equalTo(otherFileName)));
    }

    @Test
    public void shouldReturnEmptyMapWhenCreatedByNoArgConstructor() throws Exception {
        ForecastMetaData cut = new ForecastMetaData();

        Map<?, ?> headerMap = getHeaderMap(cut.asMap());
        assertThat(headerMap.isEmpty(), is(true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRequirePositiveJsonSize() throws Exception {
        new ForecastMetaData().withJsonSize(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRequirePositiveTimestamp() throws Exception {
        new ForecastMetaData().withTimestamp(0L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRequireNonNullFileName() throws Exception {
        new ForecastMetaData().withOriginalFileName(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRequireNonEmptyFileName() throws Exception {
        new ForecastMetaData().withOriginalFileName("");
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullpointerExceptionWhenIdIsNull() throws Exception {
        new ForecastMetaData(null);
    }

    private ForecastDataId getForecastIdWithType(Type type) {
        return new ForecastDataId("Some area", ForecastProvider.DMI, type);
    }

    private ForecastDataId getForecastIdWithArea(String area) {
        return new ForecastDataId(area, ForecastProvider.DMI, Type.CURRENT_FORECAST);
    }

    private ForecastDataId getForecastIdWithProvider(ForecastProvider provider) {
        return new ForecastDataId("Some area", provider, Type.CURRENT_FORECAST);
    }

    private Map<?, ?> getHeaderMap(Map<?, ?> map) {
        return (Map<?, ?>) map.get("header");
    }

}
