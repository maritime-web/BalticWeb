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
package dk.dma.embryo.dataformats.service;

import java.util.List;

import dk.dma.embryo.dataformats.model.Forecast;
import dk.dma.embryo.dataformats.model.ForecastType;
import dk.dma.embryo.dataformats.model.ForecastType.Type;

public interface ForecastService {
    List<ForecastType> getForecastTypes();

    ForecastType getForecastType(Type type);

    void reParse();

    Forecast getForecast(long id);

    List<Forecast> getForecastList(Type type);

    List<Forecast> listAvailableIceForecasts();

    List<Forecast> listAvailableWaveForecasts();

    List<Forecast> listAvailableCurrentForecasts();

}
