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
package dk.dma.embryo.config;

import dk.dma.arcticweb.dao.LogEntryService;
import dk.dma.arcticweb.service.EmbryoLogService;
import dk.dma.arcticweb.service.EmbryoLogServiceImpl;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

public class EmbryoLogConfiguration {
    @Inject
    LogEntryService logEntryService;

    @Produces
    public EmbryoLogService getLogger(InjectionPoint injectionPoint) {
        return new EmbryoLogServiceImpl(logEntryService, injectionPoint.getMember().getDeclaringClass());
    }
}
