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
