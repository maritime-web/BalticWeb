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

package dk.dma.embryo.dataformats.service;

import dk.dma.embryo.dataformats.model.Forecast;
import dk.dma.embryo.dataformats.model.ForecastDataId;
import dk.dma.embryo.dataformats.model.ForecastType;
import dk.dma.embryo.dataformats.persistence.ForecastDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Steen on 08-01-2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class ForecastPersistServiceImplTest {
    @Mock
    private ForecastDao forecastDao;

    @InjectMocks
    private ForecastPersistServiceImpl cut;

    @Mock
    private Forecast forecast;

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void shouldSaveFirstForecastWithAGivenCombinationOfProviderAreaAndType() throws Exception {
        when(forecastDao.findByProviderAreaAndType(any(), any(), any())).thenReturn(Collections.emptyList());

        cut.persist(forecast);

        verify(forecastDao).saveEntity(forecast);
    }

    @Test
    public void shouldInvalidateForecastWhenAnExistingForecastWithNewerTimestampWithDataAlreadyExists() throws Exception {
        ForecastDataId existingData = new ForecastDataId("NE", Forecast.Provider.DMI, ForecastType.Type.CURRENT_FORECAST);
        long existingTimestamp = 2;
        Forecast existingForecast = new Forecast("", existingData, ForecastType.Type.CURRENT_FORECAST, 2, Forecast.Provider.DMI, existingTimestamp, "North");
        when(forecastDao.findByProviderAreaAndType(any(), any(), any())).thenReturn(singletonList(existingForecast));

        cut.persist(forecast);

        verify(forecast).invalidate();
        verify(forecastDao).saveEntity(forecast);
    }
}