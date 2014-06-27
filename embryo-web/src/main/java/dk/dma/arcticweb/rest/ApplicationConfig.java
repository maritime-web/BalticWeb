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
package dk.dma.arcticweb.rest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import dk.dma.arcticweb.reporting.json.GreenPosRestService;
import dk.dma.embryo.common.log.LogEntryRestService;
import dk.dma.embryo.common.rs.CommonExceptionMappers;
import dk.dma.embryo.dataformats.json.IceObservationRestService;
import dk.dma.embryo.dataformats.json.ShapeFileRestService;
import dk.dma.embryo.metoc.json.MetocRestService;
import dk.dma.embryo.msi.rs.MsiRestService;
import dk.dma.embryo.user.json.AuthenticationService;
import dk.dma.embryo.user.json.ForgotPasswordRestService;
import dk.dma.embryo.user.json.RequestAccessRestService;
import dk.dma.embryo.user.json.UserRestService;
import dk.dma.embryo.vessel.json.BerthRestService;
import dk.dma.embryo.vessel.json.RouteRestService;
import dk.dma.embryo.vessel.json.RouteUploadRestService;
import dk.dma.embryo.vessel.json.ScheduleRestService;
import dk.dma.embryo.weather.json.WeatherRestService;

@ApplicationPath("/rest")
public class ApplicationConfig extends Application {
    public Set<Class<?>> getClasses() {
        HashSet<Class<?>> set = new HashSet<>();

        // ADD ExceptionMappers
        set.addAll(Arrays.asList(CommonExceptionMappers.getMappers()));

        // ADD RS ENDPOINTS
        set.addAll(Arrays.asList(RouteRestService.class, RouteUploadRestService.class, ShapeFileRestService.class,
                GreenPosRestService.class, BerthRestService.class, AuthenticationService.class,
                TestDataRestService.class, IceObservationRestService.class, MsiRestService.class,
                MetocRestService.class, ArcticWebVesselRestService.class, ScheduleRestService.class, LogEntryRestService.class,
                UserRestService.class, RequestAccessRestService.class, ForgotPasswordRestService.class, WeatherRestService.class));

        return set;
    }

}
