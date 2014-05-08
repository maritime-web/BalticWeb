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
package dk.dma.embryo.user.shiro;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.embryo.common.configuration.Configuration;
import dk.dma.embryo.user.security.Subject;
import dk.dma.embryo.vessel.persistence.ScheduleDao;

/**
 * @author Jesper Tejlgaard
 */
public class EmbryoRouteFilter extends EmbryoVesselDataFilter {

    Logger logger = LoggerFactory.getLogger(EmbryoRouteFilter.class);

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue)
            throws Exception {
        Object jsonObject = Util.getJson(request.getInputStream(), Object.class);
        String routeId = Util.getValue(jsonObject, ((String[]) mappedValue)[0]);
        
        ScheduleDao scheduleService = Configuration.getBean(ScheduleDao.class);
        Long mmsi = null;
        if(((Object[])mappedValue).length > 1){
            String voyageId = Util.getValue(jsonObject, ((String[]) mappedValue)[1]);
            mmsi = scheduleService.getMmsiByVoyageEnavId(voyageId);
        }else{
            mmsi = scheduleService.getMmsiByRouteEnavId(routeId);
        }
        
        Subject subject = Configuration.getBean(Subject.class);
        return subject.authorizedToModifyVessel(mmsi);
    }


}
