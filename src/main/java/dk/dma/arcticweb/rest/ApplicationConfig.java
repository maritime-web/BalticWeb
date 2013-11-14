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

import dk.dma.embryo.rest.AuthenticationService;
import dk.dma.embryo.rest.BerthRestService;
import dk.dma.embryo.rest.IceObservationRestService;
import dk.dma.embryo.rest.MetocRestService;
import dk.dma.embryo.rest.MsiRestService;
import dk.dma.embryo.rest.RouteRestService;
import dk.dma.embryo.rest.RouteUploadService;
import dk.dma.embryo.rest.ScheduleRestService;
import dk.dma.embryo.rest.ShapeFileService;
import dk.dma.embryo.rest.VesselRestService;

@ApplicationPath("/rest")
public class ApplicationConfig extends Application {
    public Set<Class<?>> getClasses() {
        return new HashSet<Class<?>>(Arrays.asList(RouteRestService.class, RouteUploadService.class,
                ShapeFileService.class, GreenPosRestService.class,
                BerthRestService.class, AuthenticationService.class, TestDataRestService.class,
                IceObservationRestService.class, MsiRestService.class, MetocRestService.class, VesselRestService.class,
                ScheduleRestService.class));
    }
}
