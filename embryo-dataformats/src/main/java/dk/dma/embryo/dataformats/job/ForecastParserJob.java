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

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.dataformats.service.ForecastService;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;

@Singleton
@AccessTimeout(value = 8000)
@Startup
public class ForecastParserJob {

    @Inject
    private ForecastService forecastService;

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
    public void parseFiles() {
        forecastService.reParse();
    }
}
