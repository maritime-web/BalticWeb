/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.embryo.metoc.json;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import dk.dma.embryo.metoc.json.client.DmiSejlRuteService.Forecast;
import dk.dma.embryo.metoc.json.client.DmiSejlRuteService.ForecastValue;

/**
 * @author Jesper Tejlgaard
 */
public class ForecastPredicateTest {

    private ForecastPredicate predicate = new ForecastPredicate();
    private Forecast forecast = null;

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
        assertFalse(predicate.apply(forecast));
    }
    
    @Test
    public void testWave() {
        forecast.setWaveDir(value(1.0));
        assertFalse(predicate.apply(forecast));

        forecast.setWaveHeight(value(1.0));
        assertFalse(predicate.apply(forecast));

        forecast.setWavePeriod(value(1.0));
        assertTrue(predicate.apply(forecast));
    }

    @Test
    public void testCurrent() {
        forecast.setCurrentDir(value(1.0));
        assertFalse(predicate.apply(forecast));

        forecast.setCurrentSpeed(value(1.0));
        assertTrue(predicate.apply(forecast));
    }

    @Test
    public void testWindOnly() {
        forecast.setWindDir(value(1.0));
        assertFalse(predicate.apply(forecast));

        forecast.setWindSpeed(value(1.0));
        assertTrue(predicate.apply(forecast));
    }
}
