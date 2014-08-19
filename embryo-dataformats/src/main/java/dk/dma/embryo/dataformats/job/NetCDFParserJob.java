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

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.dataformats.service.NetCDFService;

@Singleton
@Startup
public class NetCDFParserJob {

    @Inject
    private NetCDFService netCDFService;

    @Inject
    @Property("embryo.netcdf.cron")
    private ScheduleExpression cron;

    @Resource
    private TimerService timerService;

    @PostConstruct
    public void init() {
        timerService.createCalendarTimer(cron, new TimerConfig(null, false));
    }

    @Timeout
    public void parseFiles() throws IOException {
        netCDFService.parseAllFiles();
    }
}
