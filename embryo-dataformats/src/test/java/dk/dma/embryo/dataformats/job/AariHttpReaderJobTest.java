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
package dk.dma.embryo.dataformats.job;

import java.util.HashSet;

import javax.ejb.ScheduleExpression;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import dk.dma.embryo.common.configuration.PropertyFileService;

/**
 * @author Jesper Tejlgaard
 */
public class AariHttpReaderJobTest {

    @Test
    public void test() {

        PropertyFileService mock = Mockito.mock(PropertyFileService.class);
        Mockito.when(mock.getProperty("embryo.iceChart.aari.http.d0004.path")).thenReturn("dataSets/{region}/sigrid/{yyyy}");
        Mockito.when(mock.getProperty("embryo.iceChart.aari.http.d0004.regions")).thenReturn("gre;bal;bar");
        Mockito.when(mock.getProperty("embryo.iceChart.aari.http.d0015.path")).thenReturn("dataSets/arctic/sigrid/{yyyy}");
        Mockito.when(mock.getProperty("embryo.iceChart.aari.http.d0015.regions")).thenReturn(null);

        ScheduleExpression r = new ScheduleExpression();

        TimerService timerMock = Mockito.mock(TimerService.class);
        Mockito.when(timerMock.createCalendarTimer(r, new TimerConfig(null, false))).thenReturn(null);

        AariHttpReaderJob job = new AariHttpReaderJob(r, "server", "d0004;d0015", mock, timerMock);

        job.init();

        HashSet<String> paths = job.getPaths();

        Assert.assertEquals(4, paths.size());
        Assert.assertTrue(paths.contains("dataSets/gre/sigrid/{yyyy}"));
        Assert.assertTrue(paths.contains("dataSets/bal/sigrid/{yyyy}"));
        Assert.assertTrue(paths.contains("dataSets/bar/sigrid/{yyyy}"));
        Assert.assertTrue(paths.contains("dataSets/arctic/sigrid/{yyyy}"));
    }

}
