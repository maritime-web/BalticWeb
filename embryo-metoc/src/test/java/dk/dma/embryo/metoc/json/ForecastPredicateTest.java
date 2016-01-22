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
package dk.dma.embryo.metoc.json;

import dk.dma.embryo.metoc.json.client.DmiSejlRuteService.Forecast;
import dk.dma.embryo.metoc.json.client.DmiSejlRuteService.ForecastValue;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Jesper Tejlgaard
 */
public class ForecastPredicateTest {

    private ForecastPredicate predicate = new ForecastPredicate();
    private Forecast forecast;

    @Before
    public void setup() {
        forecast = new Forecast();
    }

    private ForecastValue value(Double dv) {
        ForecastValue value = new ForecastValue();
        value.setForecast(dv);
        return value;
    }

    public void testEmpty(){
        assertFalse(predicate.test(forecast));
    }
    
    @Test
    public void testWave() {
        forecast.setWaveDir(value(1.0));
        assertFalse(predicate.test(forecast));

        forecast.setWaveHeight(value(1.0));
        assertFalse(predicate.test(forecast));

        forecast.setWavePeriod(value(1.0));
        assertTrue(predicate.test(forecast));
    }

    @Test
    public void testCurrent() {
        forecast.setCurrentDir(value(1.0));
        assertFalse(predicate.test(forecast));

        forecast.setCurrentSpeed(value(1.0));
        assertTrue(predicate.test(forecast));
    }

    @Test
    public void testWindOnly() {
        forecast.setWindDir(value(1.0));
        assertFalse(predicate.test(forecast));

        forecast.setWindSpeed(value(1.0));
        assertTrue(predicate.test(forecast));
    }
}
