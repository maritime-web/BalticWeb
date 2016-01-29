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
package dk.dma.arcticweb.rest;

import dk.dma.arcticweb.reporting.json.GreenPosRestService;
import dk.dma.embryo.common.log.LogEntryRestService;
import dk.dma.embryo.common.rs.CommonExceptionMappers;
import dk.dma.embryo.dataformats.inshore.InshoreIceReportJsonService;
import dk.dma.embryo.dataformats.json.ForecastRestService;
import dk.dma.embryo.dataformats.json.IceObservationRestService;
import dk.dma.embryo.dataformats.json.ShapeFileRestService;
import dk.dma.embryo.metoc.json.MetocRestService;
import dk.dma.embryo.msi.rs.MsiRestService;
import dk.dma.embryo.tiles.json.TileSetJsonService;
import dk.dma.embryo.user.json.AreasOfInterestRestService;
import dk.dma.embryo.user.json.AuthenticationService;
import dk.dma.embryo.user.json.FeedbackRestService;
import dk.dma.embryo.user.json.ForgotPasswordRestService;
import dk.dma.embryo.user.json.RequestAccessRestService;
import dk.dma.embryo.user.json.UserRestService;
import dk.dma.embryo.vessel.json.BerthRestService;
import dk.dma.embryo.vessel.json.RouteRestService;
import dk.dma.embryo.vessel.json.RouteUploadRestService;
import dk.dma.embryo.vessel.json.ScheduleRestService;
import dk.dma.embryo.weather.json.WeatherRestService;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/rest")
public class ApplicationConfig extends Application {
    public Set<Class<?>> getClasses() {
        HashSet<Class<?>> set = new HashSet<>();

        // ADD ExceptionMappers
        set.addAll(Arrays.asList(CommonExceptionMappers.getMappers()));

        // ADD RS ENDPOINTS
        set.addAll(Arrays.asList(
                RouteRestService.class, 
                RouteUploadRestService.class, 
                ShapeFileRestService.class,
                GreenPosRestService.class, 
                BerthRestService.class, 
                AuthenticationService.class,
                TestDataRestService.class, 
                IceObservationRestService.class, 
                MsiRestService.class,
                MetocRestService.class, 
                ArcticWebVesselRestService.class, 
                ScheduleRestService.class,
                LogEntryRestService.class, 
                UserRestService.class, 
                RequestAccessRestService.class,
                ForgotPasswordRestService.class, 
                WeatherRestService.class, 
                InshoreIceReportJsonService.class, 
                ForecastRestService.class,
                FeedbackRestService.class, 
                TileSetJsonService.class,
                AreasOfInterestRestService.class));

        return set;
    }

}
